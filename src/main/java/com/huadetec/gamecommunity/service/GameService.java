package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.Game;
import com.huadetec.gamecommunity.entity.Guide;
import com.huadetec.gamecommunity.entity.Question;
import com.huadetec.gamecommunity.repository.GameRepository;
import com.huadetec.gamecommunity.service.GuideService;
import com.huadetec.gamecommunity.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * 游戏服务类
 * 实现游戏相关的业务逻辑
 */
@Service  // 标记为服务层组件
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private GuideService guideService;
    
    @Autowired
    private QuestionService questionService;

    /**
     * 添加新游戏
     * @param game 游戏对象
     * @return 添加后的游戏对象
     */
    public Game addGame(Game game) {
        // 验证游戏名称是否已存在
        if (gameRepository.existsByName(game.getName())) {
            throw new RuntimeException("游戏名称已存在：" + game.getName());
        }
        // 保存游戏信息
        return gameRepository.save(game);
    }

    /**
     * 更新游戏信息
     * @param game 游戏对象
     * @return 更新后的游戏对象
     */
    public Game updateGame(Game game) {
        // 验证游戏是否存在
        Assert.notNull(game.getId(), "游戏ID不能为空");
        Optional<Game> existingGame = gameRepository.findById(game.getId());
        if (!existingGame.isPresent()) {
            throw new RuntimeException("游戏不存在：" + game.getId());
        }
        // 保存更新后的游戏信息
        return gameRepository.save(game);
    }

    /**
     * 删除游戏
     * @param id 游戏ID
     */
    public void deleteGame(Long id) {
        // 验证游戏是否存在
        if (!gameRepository.existsById(id)) {
            throw new RuntimeException("游戏不存在：" + id);
        }
        // 删除游戏
        gameRepository.deleteById(id);
    }

    /**
     * 根据ID查找游戏
     * @param id 游戏ID
     * @return 游戏对象
     */
    public Optional<Game> findGameById(Long id) {
        return gameRepository.findById(id);
    }

    /**
     * 根据名称查找游戏
     * @param name 游戏名称
     * @return 游戏对象
     */
    public Optional<Game> findGameByName(String name) {
        return gameRepository.findByName(name);
    }

    /**
     * 获取所有游戏列表
     * @return 游戏列表
     */
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    /**
     * 获取推荐游戏列表（首页大图展示）
     * @return 推荐游戏列表
     */
    public List<Game> getFeaturedGames() {
        return gameRepository.findByIsFeaturedTrue();
    }

    /**
     * 根据分类查找游戏
     * @param category 游戏分类
     * @return 游戏列表
     */
    public List<Game> findGamesByCategory(String category) {
        return gameRepository.findByCategoriesContaining(category);
    }

    /**
     * 设置游戏为推荐或取消推荐
     * @param id 游戏ID
     * @param isFeatured 是否推荐
     * @return 更新后的游戏对象
     */
    public Game setGameFeatured(Long id, Boolean isFeatured) {
        // 验证游戏是否存在
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("游戏不存在：" + id));
        // 设置推荐状态
        game.setIsFeatured(isFeatured);
        // 保存更新后的游戏信息
        return gameRepository.save(game);
    }
    
    /**
     * 计算游戏的总浏览量（实时计算）
     * @param gameId 游戏ID
     * @return 总浏览量（攻略views + 问答view_count）
     */
    public int calculateTotalViews(Long gameId) {
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
        
        return totalViews;
    }
}