package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.Admin;
import com.huadetec.gamecommunity.entity.Game;
import com.huadetec.gamecommunity.entity.Guide;
import com.huadetec.gamecommunity.entity.OperationLog;
import com.huadetec.gamecommunity.entity.Question;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.repository.GameRepository;
import com.huadetec.gamecommunity.repository.UserRepository;
import com.huadetec.gamecommunity.service.AdminService;
import com.huadetec.gamecommunity.service.GameService;
import com.huadetec.gamecommunity.service.GuideService;
import com.huadetec.gamecommunity.service.OperationLogService;
import com.huadetec.gamecommunity.service.QuestionService;
import com.huadetec.gamecommunity.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 攻略控制器
 * 处理攻略相关的HTTP请求
 */
@RestController
// 移除@CrossOrigin注解，使用全局CORS配置
@RequestMapping("/api")
public class GuideController {

    @Autowired
    private GuideService guideService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private GameService gameService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private OperationLogService operationLogService;

    /**
     * 获取指定游戏的攻略列表
     * @param gameId 游戏ID
     * @return 攻略列表
     */
    @GetMapping("/game/{gameId}/guides")
    public ResponseEntity<Result> getGameGuides(@PathVariable Long gameId) {
        List<Guide> guides = guideService.getGuidesByGameId(gameId);
        return new ResponseEntity<>(Result.success(guides), HttpStatus.OK);
    }

    /**
     * 获取当前用户的攻略列表
     * 如果是管理员用户，则返回所有攻略
     * 否则返回当前用户自己的攻略
     * @return 攻略列表
     */
    @GetMapping("/user/guides")
    public ResponseEntity<Result> getUserGuides() {
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(Result.error(401, "未登录"), HttpStatus.UNAUTHORIZED);
        }

        List<Guide> guides;
        // 检查当前用户是否有管理员权限
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication() != null && 
                         SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                         .anyMatch(authority -> {
                             String authorityStr = authority.getAuthority();
                             return "ROLE_ADMIN".equals(authorityStr) || 
                                    "ADMIN".equals(authorityStr) || 
                                    "admin".equals(authorityStr);
                         });
        
        // 如果是管理员用户，则返回所有攻略
        if (isAdmin) {
            guides = guideService.getAllGuides();
        } else {
            // 普通用户只返回自己的攻略
            guides = guideService.getGuidesByUserId(currentUser.getId());
        }
        
        return new ResponseEntity<>(Result.success(guides), HttpStatus.OK);
    }

    /**
     * 获取按权重排序的前9个攻略
     * 权重计算公式：likes * 1 + views * 0.1
     * 允许未登录用户访问
     * @return 按权重降序排序的前9个攻略列表，包含完整的游戏信息
     */
    @GetMapping("/guides/top9")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result> getTop9GuidesByWeight() {
        try {
            List<Guide> guides = guideService.getTop9GuidesByWeight();
            
            // 构建包含完整信息的响应
            List<Map<String, Object>> responseList = new ArrayList<>();
            for (Guide guide : guides) {
                Map<String, Object> guideInfo = new HashMap<>();
                guideInfo.put("id", guide.getId());
                guideInfo.put("title", guide.getTitle());
                guideInfo.put("content", guide.getContent());
                guideInfo.put("coverImageUrl", guide.getCoverImageUrl());
                guideInfo.put("views", guide.getViews());
                guideInfo.put("likes", guide.getLikes());
                guideInfo.put("createTime", guide.getCreateTime());
                
                // 添加作者信息
                if (guide.getAuthor() != null) {
                    Map<String, Object> authorInfo = new HashMap<>();
                    authorInfo.put("username", guide.getAuthor().getUsername());
                    guideInfo.put("author", authorInfo);
                }
                
                // 添加完整的游戏信息（包含posterUrl）
                if (guide.getGame() != null) {
                    Map<String, Object> gameInfo = new HashMap<>();
                    gameInfo.put("id", guide.getGame().getId());
                    gameInfo.put("name", guide.getGame().getName());
                    gameInfo.put("description", guide.getGame().getDescription());
                    gameInfo.put("logoUrl", guide.getGame().getLogoUrl());
                    gameInfo.put("posterUrl", guide.getGame().getPosterUrl());
                    gameInfo.put("categories", guide.getGame().getCategories());
                    guideInfo.put("game", gameInfo);
                }
                
                responseList.add(guideInfo);
            }
            
            return new ResponseEntity<>(Result.success(responseList), HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("获取按权重排序的前9个攻略时发生错误: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(Result.error(500, "获取攻略列表失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取攻略详情并增加浏览量
     * 允许未登录用户访问，因为浏览攻略不需要登录
     * @param guideId 攻略ID
     * @return 攻略详情
     */
    @GetMapping("/guide/{guideId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result> getGuideDetail(@PathVariable Long guideId) {
        try {
            // 查找攻略
            Optional<Guide> guideOptional = guideService.getGuideById(guideId);
            if (!guideOptional.isPresent()) {
                return new ResponseEntity<>(Result.error(404, "攻略不存在"), HttpStatus.NOT_FOUND);
            }
            
            Guide guide = guideOptional.get();
            
            // 增加浏览量 - 简化版本，不访问关联对象
            Integer currentViews = guide.getViews();
            if (currentViews == null) {
                currentViews = 0;
            }
            guide.setViews(currentViews + 1);
            guideService.saveGuide(guide);
            
            // 计算游戏的总浏览量（攻略数、问答数、总浏览量）
            Long gameId = guide.getGame() != null ? guide.getGame().getId() : null;
            int totalViews = 0;
            int guideCount = 0;
            int questionCount = 0;
            
            if (gameId != null) {
                // 获取该游戏的攻略列表
                List<Guide> guides = guideService.getGuidesByGameId(gameId);
                guideCount = guides.size();
                
                // 获取该游戏的问答列表
                List<Question> questions = questionService.getQuestionsByGameId(gameId);
                questionCount = questions.size();
                
                // 计算总浏览量
                for (Guide g : guides) {
                    totalViews += g.getViews() != null ? g.getViews() : 0;
                }
                for (Question q : questions) {
                    totalViews += q.getViewCount() != null ? q.getViewCount() : 0;
                }
                

            }
            
            // 返回完整的攻略信息，确保前端能正确显示
            Map<String, Object> response = new HashMap<>();
            response.put("id", guide.getId());
            response.put("title", guide.getTitle());
            response.put("content", guide.getContent());
            response.put("coverImageUrl", guide.getCoverImageUrl());
            response.put("views", guide.getViews());
            response.put("likes", guide.getLikes());
            response.put("createTime", guide.getCreateTime());
            
            // 添加作者信息
            if (guide.getAuthor() != null) {
                Map<String, Object> authorInfo = new HashMap<>();
                authorInfo.put("username", guide.getAuthor().getUsername());
                response.put("author", authorInfo);
            }
            
            // 添加游戏信息（包含总浏览量）
            if (guide.getGame() != null) {
                Map<String, Object> gameInfo = new HashMap<>();
                gameInfo.put("id", guide.getGame().getId());
                gameInfo.put("name", guide.getGame().getName());
                gameInfo.put("totalViews", totalViews);
                gameInfo.put("guideCount", guideCount);
                gameInfo.put("questionCount", questionCount);
                response.put("game", gameInfo);
            }
            
            return new ResponseEntity<>(Result.success(response), HttpStatus.OK);
        } catch (Exception e) {
            // 简化错误处理，只记录错误信息
            System.err.println("获取攻略详情时发生错误: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(Result.error(500, "获取攻略详情失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 发布新攻略
     * @param guideData 攻略数据（包含title, content, gameId）
     * @return 成功或失败信息
     */
    @PostMapping("/guide/create")
    public ResponseEntity<Result> createGuide(@RequestBody Map<String, Object> guideData) {
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(Result.error(401, "请先登录"), HttpStatus.UNAUTHORIZED);
        }

        try {
            // 获取攻略标题、内容和游戏ID
            String title = (String) guideData.get("title");
            String content = (String) guideData.get("content");
            Long gameId = Long.parseLong(guideData.get("gameId").toString());

            // 验证参数
            if (title == null || title.trim().isEmpty() || title.length() > 200) {
                return new ResponseEntity<>(Result.error(400, "标题不能为空且不能超过200个字符"), HttpStatus.BAD_REQUEST);
            }

            if (content == null || content.trim().isEmpty()) {
                return new ResponseEntity<>(Result.error(400, "攻略内容不能为空"), HttpStatus.BAD_REQUEST);
            }

            // 查找游戏
            Optional<Game> gameOptional = gameRepository.findById(gameId);
            if (!gameOptional.isPresent()) {
                return new ResponseEntity<>(Result.error(400, "游戏不存在"), HttpStatus.BAD_REQUEST);
            }

            // 创建新攻略
            Guide newGuide = new Guide();
            newGuide.setTitle(title);
            newGuide.setContent(content);
            // 处理封面图片URL
            if (guideData.containsKey("coverImageUrl")) {
                String coverImageUrl = (String) guideData.get("coverImageUrl");
                newGuide.setCoverImageUrl(coverImageUrl);
            }
            newGuide.setAuthor(currentUser);
            newGuide.setGame(gameOptional.get());

            // 保存攻略
            Guide savedGuide = guideService.saveGuide(newGuide);

            // 返回成功信息
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "攻略发布成功");
            response.put("guide", savedGuide);
            return new ResponseEntity<>(Result.success(response), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Result.error(500, "发布失败：" + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除攻略
     * @param guideId 攻略ID
     * @return 成功或失败信息
     */
    @DeleteMapping("/guide/{guideId}")
    public ResponseEntity<Result> deleteGuide(@PathVariable Long guideId, HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(Result.error(401, "请先登录"), HttpStatus.UNAUTHORIZED);
        }

        try {
            // 查找攻略
            Optional<Guide> guideOptional = guideService.getGuideById(guideId);
            if (!guideOptional.isPresent()) {
                return new ResponseEntity<>(Result.error(404, "攻略不存在"), HttpStatus.NOT_FOUND);
            }

            Guide guide = guideOptional.get();
            // 检查是否是攻略作者
            boolean isAuthor = guide.getAuthor().getId().equals(currentUser.getId());
            
            // 检查当前用户是否有管理员权限（从SecurityContext获取）
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication() != null && 
                             SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                             .anyMatch(authority -> {
                                 String authorityStr = authority.getAuthority();
                                 return "ROLE_ADMIN".equals(authorityStr) || 
                                        "ADMIN".equals(authorityStr) || 
                                        "admin".equals(authorityStr);
                             });
            
            // 如果用户是管理员，记录管理员操作日志
            if (isAdmin) {
                // 获取当前登录管理员信息
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null) {
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    String adminUsername = userDetails.getUsername();
                    
                    // 调用getAdminIdFromUsername方法
                    Long adminId = getAdminIdFromUsername(adminUsername);
                    
                    // 记录操作日志
                    operationLogService.logSuccessOperation(
                        adminId, 
                        adminUsername, 
                        "DELETE_GUIDE", 
                        "GUIDE", 
                        guideId, 
                        guide.getTitle(), 
                        "删除用户攻略：" + guide.getTitle() + " (作者：" + guide.getAuthor().getUsername() + ")", 
                        request
                    );
                }
            }
            
            if (!isAuthor && !isAdmin) {
                return new ResponseEntity<Result>(Result.error(403, "只能删除自己发布的攻略"), HttpStatus.FORBIDDEN);
            }

            // 删除攻略
            boolean deleteResult = guideService.deleteGuide(guideId, currentUser);

            // 返回成功信息
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "攻略删除成功");
            return new ResponseEntity<Result>(Result.success(response), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Result>(Result.error(500, "删除失败：" + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * 点赞攻略
     * @param guideId 攻略ID
     * @return 成功或失败信息
     */
    @PostMapping("/guide/{guideId}/like")
    public ResponseEntity<Result> likeGuide(@PathVariable Long guideId) {
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<Result>(Result.error(401, "请先登录"), HttpStatus.UNAUTHORIZED);
        }

        try {
            // 调用服务层方法进行点赞，传递当前登录用户
            int newLikesCount = guideService.likeGuide(guideId, currentUser);
            
            // 返回成功信息和更新后的点赞数
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "点赞成功");
            response.put("likes", newLikesCount);
            return new ResponseEntity<Result>(Result.success(response), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Result>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 检查用户是否已经点赞了指定攻略
     * @param guideId 攻略ID
     * @return 是否已点赞
     */
    @GetMapping("/guide/{guideId}/check-like")
    public ResponseEntity<Result> checkUserLikedGuide(@PathVariable Long guideId) {
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            // 未登录用户视为未点赞
            Map<String, Object> response = new HashMap<>();
            response.put("isLiked", false);
            return new ResponseEntity<Result>(Result.success(response), HttpStatus.OK);
        }

        try {
            // 调用服务层方法检查是否已点赞
            boolean isLiked = guideService.checkUserLikedGuide(guideId, currentUser);
            
            // 返回检查结果
            Map<String, Object> response = new HashMap<>();
            response.put("isLiked", isLiked);
            return new ResponseEntity<Result>(Result.success(response), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<Result>(Result.error(500, "检查点赞状态失败：" + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 编辑攻略
     * @param guideId 攻略ID
     * @param guideData 攻略数据（包含title, content, gameId等）
     * @return 成功或失败信息
     */
    @PutMapping("/guide/{guideId}")
    public ResponseEntity<Result> updateGuide(@PathVariable Long guideId, @RequestBody Map<String, Object> guideData) {
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<Result>(Result.error(401, "请先登录"), HttpStatus.UNAUTHORIZED);
        }

        try {
            // 获取攻略标题、内容和游戏ID
            String title = (String) guideData.get("title");
            String content = (String) guideData.get("content");
            String coverImageUrl = (String) guideData.get("coverImageUrl");
            Long gameId = guideData.containsKey("gameId") ? Long.parseLong(guideData.get("gameId").toString()) : null;

            // 使用GuideService中的updateGuide方法更新攻略
            Guide updatedGuide = guideService.updateGuide(guideId, title, content, coverImageUrl, gameId, currentUser);

            // 如果提供了新的游戏ID，更新游戏信息
            if (gameId != null) {
                Optional<Game> gameOptional = gameRepository.findById(gameId);
                if (!gameOptional.isPresent()) {
                    return new ResponseEntity<Result>(Result.error(400, "游戏不存在"), HttpStatus.BAD_REQUEST);
                }
                updatedGuide.setGame(gameOptional.get());
                // 保存更新后的攻略（包含游戏信息）
                updatedGuide = guideService.saveGuide(updatedGuide);
            }

            // 返回成功信息
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "攻略编辑成功");
            response.put("guide", updatedGuide);
            return new ResponseEntity<Result>(Result.success(response), HttpStatus.OK);
        } catch (RuntimeException e) {
            // 处理业务逻辑异常
            if (e.getMessage().contains("攻略不存在")) {
                return new ResponseEntity<Result>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
            } else if (e.getMessage().contains("没有权限")) {
                return new ResponseEntity<Result>(Result.error(403, e.getMessage()), HttpStatus.FORBIDDEN);
            } else if (e.getMessage().contains("标题不能为空") || e.getMessage().contains("攻略内容不能为空")) {
                return new ResponseEntity<Result>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<Result>(Result.error(500, "编辑失败：" + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<Result>(Result.error(500, "编辑失败：" + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取当前登录用户
     * @return 当前登录用户对象
     */
    private User getCurrentUser() {
        try {
            // 获取认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }
            
            Object principal = authentication.getPrincipal();
            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }
            
            // 首先尝试从user表查找用户
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                return userOptional.get();
            }
            
            // 如果user表中没找到，尝试从admin_user表查找管理员用户
            Optional<Admin> adminOptional = adminService.findByUsername(username);
            if (adminOptional.isPresent()) {
                // 为管理员用户创建一个临时的User对象
                Admin admin = adminOptional.get();
                User adminUser = new User();
                adminUser.setId(admin.getId());
                adminUser.setUsername(admin.getUsername());
                adminUser.setPassword(admin.getPassword());
                adminUser.setEmail(admin.getEmail());
                // 不需要设置role，管理员权限已在Spring Security认证信息中处理
                return adminUser;
            }
            
            System.out.println("未找到用户: " + username);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取当前用户失败: " + e.getMessage());
            return null;
        }
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
}