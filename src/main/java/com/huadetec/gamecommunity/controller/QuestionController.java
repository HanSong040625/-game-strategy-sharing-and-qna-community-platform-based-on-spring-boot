package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.Answer;
import com.huadetec.gamecommunity.entity.Game;
import com.huadetec.gamecommunity.entity.Question;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.service.AnswerService;
import com.huadetec.gamecommunity.service.GameService;
import com.huadetec.gamecommunity.service.QuestionService;
import com.huadetec.gamecommunity.service.UserService;
import com.huadetec.gamecommunity.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.Optional;
import com.huadetec.gamecommunity.util.Result;

/**
 * 问答控制器
 * 处理问答相关的API请求
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private GameService gameService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AdminService adminService;

    /**
     * 获取问题列表（支持分页）
     * @param gameId 游戏ID（可选）
     * @param page 当前页码
     * @param pageSize 每页数量
     * @return 问题列表及总数
     */
    @GetMapping
    public ResponseEntity<Result<Map<String, Object>>> getQuestionList(
            @RequestParam(required = false) Long gameId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        logger.info("获取问题列表，gameId: {}, 页码: {}, 每页数量: {}", gameId, page, pageSize);
        try {
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            
            if (gameId != null) {
                // 根据游戏ID查询问题
                List<Question> questions = questionService.getQuestionsByGameId(gameId);
                data.put("questions", questions);
                data.put("total", questions.size());
            } else {
                // 查询所有问题（简化版，实际应实现分页查询）
                List<Question> allQuestions = questionService.getQuestionsByGameId(null);
                data.put("questions", allQuestions);
                data.put("total", allQuestions.size());
            }
            
            return new ResponseEntity<>(Result.success(data), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取问题列表失败", e);
            return new ResponseEntity<>(Result.error(500, "获取问题列表失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据游戏ID获取问题列表
     * @param gameId 游戏ID
     * @return 问题列表
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<Result<List<Question>>> getQuestionsByGame(@PathVariable Long gameId) {
        logger.info("根据游戏ID获取问题列表，gameId: {}", gameId);
        try {
            List<Question> questions = questionService.getQuestionsByGameId(gameId);
            return new ResponseEntity<>(Result.success(questions), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取游戏问题列表失败", e);
            return new ResponseEntity<>(Result.error(500, "获取游戏问题列表失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据用户ID获取问题列表
     * @param userId 用户ID
     * @return 问题列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Result<List<Question>>> getQuestionsByUser(@PathVariable Long userId) {
        logger.info("根据用户ID获取问题列表，userId: {}", userId);
        try {
            List<Question> questions = questionService.getQuestionsByUserId(userId);
            return new ResponseEntity<>(Result.success(questions), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取用户问题列表失败", e);
            return new ResponseEntity<>(Result.error(500, "获取用户问题列表失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 搜索问题
     * @param keyword 搜索关键词
     * @return 问题列表
     */
    @GetMapping("/search")
    public ResponseEntity<Result<List<Question>>> searchQuestions(@RequestParam String keyword) {
        logger.info("搜索问题，关键词: {}", keyword);
        try {
            List<Question> questions = questionService.searchQuestions(keyword);
            return new ResponseEntity<>(Result.success(questions), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("搜索问题失败", e);
            return new ResponseEntity<>(Result.error(500, "搜索问题失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取问题详情
     * @param id 问题ID
     * @return 问题详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Question>> getQuestionDetail(@PathVariable Long id) {
        logger.info("获取问题详情，ID: {}", id);
        try {
            Optional<Question> question = questionService.getQuestionById(id);
            if (question.isPresent()) {
                // 增加浏览量
                questionService.incrementViewCount(id);
                return new ResponseEntity<>(Result.success(question.get()), HttpStatus.OK);
            } else {
                logger.warn("问题不存在，ID: {}", id);
                return new ResponseEntity<>(Result.error(404, "问题不存在"), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("获取问题详情失败", e);
            return new ResponseEntity<>(Result.error(500, "获取问题详情失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 创建新问题
     * @param question 问题对象
     * @return 创建后的问题
     */
    @PostMapping
    public ResponseEntity<Result<Question>> createQuestion(@RequestBody Question question) {
        logger.info("创建新问题");
        try {
            Question createdQuestion = questionService.createQuestion(question);
            return new ResponseEntity<>(Result.success(createdQuestion), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("创建问题失败", e);
            return new ResponseEntity<>(Result.error(500, "创建问题失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理前端提交的问题创建请求（兼容前端路径）
     * 注意：这个方法使用了不同的路径前缀，因为控制器已经有了 @RequestMapping("/api/questions")
     */
    @RequestMapping(value = "/question/create", method = RequestMethod.POST)
    public ResponseEntity<Result<Question>> createQuestionFromFrontend(@RequestBody Map<String, Object> questionData) {
        logger.info("处理前端提交的问题创建请求，请求数据: {}", questionData);
        try {
            // 创建问题对象
            Question question = new Question();
            question.setTitle((String) questionData.get("title"));
            question.setContent((String) questionData.get("content"));
            
            // 设置作者和游戏对象
            try {
                // 获取当前登录用户
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                    // 获取当前登录用户信息
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    String username = userDetails.getUsername();
                    
                    // 检查用户是普通用户还是管理员用户
                    User currentUser = null;
                    try {
                        // 首先尝试从普通用户表查询
                        currentUser = userService.findByUsername(username).orElse(null);
                        
                        // 如果不是普通用户，检查是否是管理员用户
                        if (currentUser == null) {
                            logger.info("当前用户不是普通用户，检查是否是管理员用户");
                            
                            // 管理员用户应该从user表中查找，如果不存在则返回null
                            currentUser = null;
                        } else {
                            logger.info("设置普通用户为作者: {}", username);
                        }
                    } catch (Exception e) {
                        logger.error("查询用户信息时出错: " + e.getMessage());
                        
                        // 查询失败，返回null
                        currentUser = null;
                    }
                    
                    question.setAuthor(currentUser);
                } else {
                    logger.warn("User not logged in, cannot set author information");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Result.error(401, "User not logged in"));
                }
                
                // 尝试获取第一个可用的游戏
                List<Game> games = gameService.getAllGames();
                if (!games.isEmpty()) {
                    Game game = games.get(0);
                    question.setGame(game);
                    logger.info("使用数据库中存在的游戏ID: " + game.getId());
                } else {
                    logger.info("数据库中没有游戏数据，无法设置游戏对象");
                }
            } catch (Exception e) {
                logger.error("Error setting user and game data: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Result.error(500, "Internal server error"));
            }
            
            // 注意：Question实体类中没有imageUrls字段，所以这里不处理图片URLs
            // 如果需要支持图片上传，需要在Question实体类中添加相应字段
            if (questionData.containsKey("imageUrls")) {
                logger.info("收到图片URLs，但实体类不支持此字段: {}", questionData.get("imageUrls"));
            }
            
            logger.info("准备创建问题对象: {}", question);
            // 创建问题
            Question createdQuestion = questionService.createQuestion(question);
            logger.info("问题创建成功，ID: {}", createdQuestion.getId());
            return new ResponseEntity<>(Result.success(createdQuestion), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("创建问题失败: {}", e.getMessage(), e);
            // 返回更具体的错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, e.getMessage()));
        }
    }

    /**
     * 更新问题
     * @param id 问题ID
     * @param question 问题对象
     * @return 更新后的问题
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Question>> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        logger.info("更新问题，ID: {}", id);
        try {
            question.setId(id); // 确保ID一致
            Question updatedQuestion = questionService.updateQuestion(question);
            return new ResponseEntity<>(Result.success(updatedQuestion), HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.warn("更新问题失败: {}", e.getMessage());
            if (e.getMessage().contains("不存在")) {
                return new ResponseEntity<>(Result.error(404, "问题不存在"), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(Result.error(400, "更新问题失败"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("更新问题失败", e);
            return new ResponseEntity<>(Result.error(500, "更新问题失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除问题
     * @param id 问题ID
     * @return 响应状态
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<?>> deleteQuestion(@PathVariable Long id) {
        logger.info("删除问题，ID: {}", id);
        try {
            questionService.deleteQuestion(id);
            return new ResponseEntity<>(Result.success(null), HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            logger.warn("删除问题失败: {}", e.getMessage());
            if (e.getMessage().contains("不存在")) {
                return new ResponseEntity<>(Result.error(404, "问题不存在"), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(Result.error(400, "删除问题失败"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("删除问题失败", e);
            return new ResponseEntity<>(Result.error(500, "删除问题失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 为问题点赞/踩
     * @param id 问题ID
     * @param isLike 是否点赞
     * @return 响应状态
     */
    @PostMapping("/{id}/vote")
    public ResponseEntity<Result<?>> voteQuestion(@PathVariable Long id, @RequestParam boolean isLike) {
        logger.info("为问题点赞/踩，ID: {}，isLike: {}", id, isLike);
        try {
            questionService.voteQuestion(id, isLike);
            return new ResponseEntity<>(Result.success(null), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("点赞/踩问题失败", e);
            return new ResponseEntity<>(Result.error(500, "点赞/踩问题失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取问题的回答列表
     * @param id 问题ID
     * @return 回答列表
     */
    @GetMapping("/{id}/answers")
    public ResponseEntity<Result<List<Answer>>> getQuestionAnswers(@PathVariable Long id) {
        logger.info("获取问题的回答列表，问题ID: {}", id);
        try {
            List<Answer> answers = answerService.getAnswersByQuestionId(id);
            return new ResponseEntity<>(Result.success(answers), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取回答列表失败", e);
            return new ResponseEntity<>(Result.error(500, "获取回答列表失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 为问题添加回答
     * @param id 问题ID
     * @param answer 回答对象
     * @return 创建后的回答
     */
    @PostMapping("/{id}/answers")
    public ResponseEntity<Result<Answer>> addAnswer(@PathVariable Long id, @RequestBody Answer answer) {
        logger.info("为问题添加回答，问题ID: {}", id);
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                // 获取当前登录用户信息
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                
                // 检查用户是普通用户还是管理员用户
                User currentUser = null;
                try {
                    // 首先尝试从普通用户表查询
                    currentUser = userService.findByUsername(username).orElse(null);
                    
                    // 如果不是普通用户，检查是否是管理员用户
                    if (currentUser == null) {
                        logger.info("当前用户不是普通用户，检查是否是管理员用户");
                        
                        // 管理员用户应该从user表中查找，如果不存在则返回null
                        currentUser = null;
                    } else {
                        logger.info("设置普通用户为作者: {}", username);
                    }
                } catch (Exception e) {
                    logger.error("查询用户信息时出错: " + e.getMessage());
                    
                    // 创建一个临时用户对象作为备用
                    currentUser = new User();
                    currentUser.setUsername(username);
                    currentUser.setId(1L); // 使用默认ID
                }
                
                answer.setAuthor(currentUser);
            } else {
                logger.warn("User not logged in, cannot set author information");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "User not logged in"));
            }
            
            // 确保回答关联到正确的问题
            Question question = new Question();
            question.setId(id);
            answer.setQuestion(question);
            
            Answer createdAnswer = answerService.createAnswer(answer);
            return new ResponseEntity<>(Result.success(createdAnswer), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("添加回答失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, e.getMessage()));
        }
    }

    /**
     * 采纳回答
     * @param id 问题ID
     * @param answerId 回答ID
     * @return 响应状态
     */
    @PostMapping("/{id}/accept-answer/{answerId}")
    public ResponseEntity<Result<?>> acceptAnswer(@PathVariable Long id, @PathVariable Long answerId) {
        logger.info("采纳回答，问题ID: {}，回答ID: {}", id, answerId);
        try {
            answerService.acceptAnswer(answerId, id);
            return new ResponseEntity<>(Result.success(null), HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.warn("采纳回答失败: {}", e.getMessage());
            if (e.getMessage().contains("不存在")) {
                return new ResponseEntity<>(Result.error(404, "问题或回答不存在"), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(Result.error(400, "采纳回答失败"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("采纳回答失败", e);
            return new ResponseEntity<>(Result.error(500, "采纳回答失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 为回答点赞/踩
     * @param answerId 回答ID
     * @param isLike 是否点赞
     * @return 响应状态
     */
    @PostMapping("/answer/vote")
    public ResponseEntity<Result<?>> voteAnswer(@RequestParam Long answerId, @RequestParam boolean isLike) {
        logger.info("为回答点赞/踩，回答ID: {}，isLike: {}", answerId, isLike);
        try {
            answerService.voteAnswer(answerId, isLike);
            return new ResponseEntity<>(Result.success(null), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("点赞/踩回答失败", e);
            return new ResponseEntity<>(Result.error(500, "点赞/踩回答失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}