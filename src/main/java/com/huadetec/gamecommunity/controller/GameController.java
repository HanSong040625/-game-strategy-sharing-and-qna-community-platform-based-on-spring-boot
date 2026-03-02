package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.Admin;
import com.huadetec.gamecommunity.entity.Game;
import com.huadetec.gamecommunity.entity.Guide;
import com.huadetec.gamecommunity.entity.Question;
import com.huadetec.gamecommunity.service.AdminService;
import com.huadetec.gamecommunity.service.GameService;
import com.huadetec.gamecommunity.service.GuideService;
import com.huadetec.gamecommunity.service.OperationLogService;
import com.huadetec.gamecommunity.service.QuestionService;
import com.huadetec.gamecommunity.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 游戏控制器
 * 处理游戏相关的HTTP请求
 */
@RestController  // 标记为REST控制器
@RequestMapping("/api/game")  // 基础请求路径
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameService gameService;
    
    @Autowired
    private GuideService guideService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private AdminService adminService;

    /**
     * 获取所有游戏列表（前台接口，无需登录）
     * @return 游戏列表
     */
    @GetMapping("/list")
    public ResponseEntity<Result> getAllGames() {
        logger.info("获取所有游戏列表");
        List<Game> games = gameService.getAllGames();
        // 为每个游戏设置实时计算的总浏览量
        for (Game game : games) {
            int totalViews = gameService.calculateTotalViews(game.getId());
            game.setTotalViews(totalViews);
        }
        return new ResponseEntity<>(Result.success(games), HttpStatus.OK);
    }

    /**
     * 获取推荐游戏列表（首页大图展示）（前台接口，无需登录）
     * @return 推荐游戏列表
     */
    @GetMapping("/featured")
    public ResponseEntity<Result> getFeaturedGames() {
        logger.info("获取推荐游戏列表");
        List<Game> games = gameService.getFeaturedGames();
        // 为每个游戏设置实时计算的总浏览量
        for (Game game : games) {
            int totalViews = gameService.calculateTotalViews(game.getId());
            game.setTotalViews(totalViews);
        }
        return new ResponseEntity<>(Result.success(games), HttpStatus.OK);
    }

    /**
     * 根据分类获取游戏列表（前台接口，无需登录）
     * @param category 游戏分类
     * @return 游戏列表
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Result> getGamesByCategory(@PathVariable String category) {
        logger.info("根据分类获取游戏列表：{}", category);
        List<Game> games = gameService.findGamesByCategory(category);
        // 为每个游戏设置实时计算的总浏览量
        for (Game game : games) {
            int totalViews = gameService.calculateTotalViews(game.getId());
            game.setTotalViews(totalViews);
        }
        return new ResponseEntity<>(Result.success(games), HttpStatus.OK);
    }

    /**
     * 根据ID获取游戏详情（前台接口，无需登录）
     * @param id 游戏ID
     * @return 游戏对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result> getGameById(@PathVariable Long id) {
        logger.info("根据ID获取游戏详情：{}", id);
        try {
            Game game = gameService.findGameById(id)
                    .orElseThrow(() -> new RuntimeException("游戏不存在：" + id));
            // 计算总浏览量：攻略浏览量 + 问答浏览量
            int totalViews = calculateTotalViews(id);
            game.setTotalViews(totalViews);
            return new ResponseEntity<>(Result.success(game), HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("获取游戏详情失败", e);
            return new ResponseEntity<>(Result.error(404, "游戏不存在"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("获取游戏详情失败", e);
            return new ResponseEntity<>(Result.error(500, "获取游戏详情失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 计算游戏的总浏览量
     * @param gameId 游戏ID
     * @return 总浏览量（攻略views + 问答view_count）
     */
    private int calculateTotalViews(Long gameId) {
        int totalViews = 0;
        
        // 计算攻略的总浏览量
        List<Guide> guides = guideService.getGuidesByGameId(gameId);
        for (Guide guide : guides) {
            totalViews += guide.getViews();
        }
        
        // 计算问答的总浏览量
        List<Question> questions = questionService.getQuestionsByGameId(gameId);
        for (Question question : questions) {
            totalViews += question.getViewCount();
        }
        
        logger.info("游戏ID: {} 的总浏览量计算完成: {}", gameId, totalViews);
        return totalViews;
    }

    /**
     * 添加新游戏（管理员接口，需要ADMIN角色权限）
     * @param game 游戏对象
     * @return 添加后的游戏对象
     */
    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result> addGame(@RequestBody Game game, HttpServletRequest request) {
        logger.info("添加新游戏：{}", game.getName());
        
        // 获取当前登录管理员信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String adminUsername = userDetails.getUsername();
        
        Game savedGame = gameService.addGame(game);
        
        // 记录操作日志
        operationLogService.logSuccessOperation(
            getAdminIdFromUsername(adminUsername), 
            adminUsername, 
            "ADD_GAME", 
            "GAME", 
            savedGame.getId(), 
            savedGame.getName(), 
            "添加新游戏：" + savedGame.getName(), 
            request
        );
        
       return new ResponseEntity<>(Result.success(savedGame), HttpStatus.CREATED);
    }
    
    /**
     * 根据管理员用户名获取管理员ID
     * @param username 管理员用户名
     * @return 管理员ID
     */
    private Long getAdminIdFromUsername(String username) {
        Optional<Admin> adminOptional = adminService.findByUsername(username);
        return adminOptional.map(Admin::getId).orElse(null);
    }

    /**
     * 更新游戏信息（管理员接口，需要ADMIN角色权限）
     * @param game 游戏对象
     * @return 更新后的游戏对象
     */
    @PutMapping("/admin/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result> updateGame(@RequestBody Game game, HttpServletRequest request) {
        logger.info("更新游戏信息，游戏ID：{}", game.getId());
        
        // 获取当前登录管理员信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String adminUsername = userDetails.getUsername();
        
        Game updatedGame = gameService.updateGame(game);
        
        // 记录操作日志
        operationLogService.logSuccessOperation(
            getAdminIdFromUsername(adminUsername), 
            adminUsername, 
            "UPDATE_GAME", 
            "GAME", 
            updatedGame.getId(), 
            updatedGame.getName(), 
            "更新游戏信息：" + updatedGame.getName(), 
            request
        );
        
        return new ResponseEntity<>(Result.success(updatedGame), HttpStatus.OK);
    }

    /**
     * 删除游戏（管理员接口，需要ADMIN角色权限）
     * @param id 游戏ID
     * @return 删除结果
     */
    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result> deleteGame(@PathVariable Long id, HttpServletRequest request) {
        logger.info("删除游戏，游戏ID：{}", id);
        
        // 获取当前登录管理员信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String adminUsername = userDetails.getUsername();
        
        // 先获取游戏信息用于日志记录
        Optional<Game> gameOptional = gameService.findGameById(id);
        if (!gameOptional.isPresent()) {
            // 记录失败操作日志
            operationLogService.logFailedOperation(
                getAdminIdFromUsername(adminUsername), 
                adminUsername, 
                "DELETE_GAME", 
                "GAME", 
                id, 
                "未知游戏", 
                "删除游戏失败：游戏不存在", 
                "游戏不存在",
                request
            );
            return new ResponseEntity<>(Result.error(404, "游戏不存在"), HttpStatus.NOT_FOUND);
        }
        Game game = gameOptional.get();
        
        gameService.deleteGame(id);
        
        // 记录操作日志
        operationLogService.logSuccessOperation(
            getAdminIdFromUsername(adminUsername), 
            adminUsername, 
            "DELETE_GAME", 
            "GAME", 
            id, 
            game.getName(), 
            "删除游戏：" + game.getName(), 
            request
        );
        
        return new ResponseEntity<>(Result.success("游戏删除成功"), HttpStatus.OK);
    }

    /**
     * 设置游戏推荐状态（管理员接口，需要ADMIN角色权限）
     * @param id 游戏ID
     * @param requestBody 请求体（包含isFeatured字段）
     * @return 更新后的游戏对象
     */
    @PatchMapping("/admin/featured/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result> setGameFeatured(@PathVariable Long id, @RequestBody Map<String, Boolean> requestBody, HttpServletRequest request) {
        logger.info("设置游戏推荐状态，ID：{}", id);
        
        // 获取当前登录管理员信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String adminUsername = userDetails.getUsername();
        
        Boolean isFeatured = requestBody.get("isFeatured");
        Game updatedGame = gameService.setGameFeatured(id, isFeatured);
        
        // 记录操作日志
        String operationType = isFeatured ? "RECOMMEND_GAME" : "UNRECOMMEND_GAME";
        String operationDesc = isFeatured ? "设置游戏推荐：" : "取消游戏推荐：";
        
        operationLogService.logSuccessOperation(
            getAdminIdFromUsername(adminUsername), 
            adminUsername, 
            operationType, 
            "GAME", 
            updatedGame.getId(), 
            updatedGame.getName(), 
            operationDesc + updatedGame.getName(), 
            request
        );
        
        return new ResponseEntity<>(Result.success(updatedGame), HttpStatus.OK);
    }

    /**
     * 获取所有游戏列表（管理员接口，需要ADMIN角色权限）
     * @return 游戏列表
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result> getAllGamesForAdmin() {
        logger.info("管理员获取所有游戏列表");
        List<Game> games = gameService.getAllGames();
        return new ResponseEntity<>(Result.success(games), HttpStatus.OK);
    }
}