package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.Comment;
import com.huadetec.gamecommunity.entity.Guide;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.repository.GuideRepository;
import com.huadetec.gamecommunity.repository.UserRepository;
import com.huadetec.gamecommunity.service.CommentService;
import com.huadetec.gamecommunity.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 评论控制器
 * 处理评论相关的HTTP请求
 */
@RestController
@RequestMapping("/api/guide")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;
    private final UserRepository userRepository;
    private final GuideRepository guideRepository;

    @Autowired
    public CommentController(CommentService commentService, UserRepository userRepository, GuideRepository guideRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
        this.guideRepository = guideRepository;
    }

    /**
     * 获取攻略的评论列表
     * @param guideId 攻略ID
     * @return 评论列表
     */
    @GetMapping("/{guideId}/comments")
    public ResponseEntity<Result> getGuideComments(@PathVariable Long guideId) {
        logger.info("获取攻略的评论列表，攻略ID: {}", guideId);
        try {
            List<Comment> comments = commentService.getCommentsByGuideId(guideId);
            return new ResponseEntity<>(Result.success(comments), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Result.error(500, "获取评论列表失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 创建评论
     * @param guideId 攻略ID
     * @param commentData 评论数据
     * @return 创建的评论
     */
    @PostMapping("/{guideId}/comments")
    public ResponseEntity<Result> createComment(@PathVariable Long guideId, @RequestBody Map<String, Object> commentData) {
        logger.info("创建评论，攻略ID: {}, 评论数据: {}", guideId, commentData);
        
        try {
            // 检查攻略是否存在
            Optional<Guide> optionalGuide = guideRepository.findById(guideId);
            if (!optionalGuide.isPresent()) {
                logger.warn("攻略不存在，ID: {}", guideId);
                return new ResponseEntity<>(Result.error(404, "攻略不存在"), HttpStatus.NOT_FOUND);
            }
            
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
                logger.warn("用户未登录，无法创建评论");
                return new ResponseEntity<>(Result.error(401, "请先登录"), HttpStatus.UNAUTHORIZED);
            }
            
            // 获取用户信息
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Optional<User> optionalUser = userRepository.findByUsername(username);
            
            if (!optionalUser.isPresent()) {
                logger.warn("用户不存在，用户名: {}", username);
                return new ResponseEntity<>(Result.error(401, "用户不存在"), HttpStatus.UNAUTHORIZED);
            }
            
            // 创建评论对象
            Comment comment = new Comment();
            comment.setContent((String) commentData.get("content"));
            comment.setAuthor(optionalUser.get());
            comment.setGuide(optionalGuide.get());
            
            // 保存评论
            Comment createdComment = commentService.createComment(comment);
            return new ResponseEntity<>(Result.success(createdComment), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Result.error(500, "创建评论失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 为评论点赞
     * @param commentId 评论ID
     * @return 响应状态
     */
    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<Result> likeComment(@PathVariable Long commentId) {
        logger.info("为评论点赞，评论ID: {}", commentId);
        
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
                logger.warn("用户未登录，无法点赞");
                return new ResponseEntity<>(Result.error(401, "请先登录"), HttpStatus.UNAUTHORIZED);
            }
            
            // 获取用户信息
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Optional<User> optionalUser = userRepository.findByUsername(username);
            
            if (!optionalUser.isPresent()) {
                logger.warn("用户不存在，用户名: {}", username);
                return new ResponseEntity<>(Result.error(401, "用户不存在"), HttpStatus.UNAUTHORIZED);
            }
            
            User currentUser = optionalUser.get();
            
            // 调用服务层方法进行点赞，传入当前用户
            commentService.likeComment(commentId, currentUser);
            
            return new ResponseEntity<>(Result.success(null), HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.warn("点赞失败: {}", e.getMessage());
            if (e.getMessage().contains("不存在")) {
                return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("点赞失败", e);
            return new ResponseEntity<>(Result.error(500, "点赞失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取单个评论详情
     * @param commentId 评论ID
     * @return 评论详情
     */
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<Result> getCommentById(@PathVariable Long commentId) {
        logger.info("获取评论详情，评论ID: {}", commentId);
        try {
            Optional<Comment> comment = commentService.getCommentById(commentId);
            if (comment.isPresent()) {
                return new ResponseEntity<>(Result.success(comment.get()), HttpStatus.OK);
            } else {
                logger.warn("评论不存在，评论ID: {}", commentId);
                return new ResponseEntity<>(Result.error(404, "评论不存在"), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("获取评论详情失败", e);
            return new ResponseEntity<>(Result.error(500, "获取评论详情失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除评论（如果需要管理员或评论作者才能删除）
     * @param commentId 评论ID
     * @return 响应状态
     */
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Result> deleteComment(@PathVariable Long commentId) {
        logger.info("删除评论，评论ID: {}", commentId);
        try {
            // 这里可以添加权限检查逻辑，例如只有管理员或评论作者才能删除
            // 暂时简化处理，直接删除
            commentService.deleteComment(commentId);
            return new ResponseEntity<>(Result.success(null), HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.warn("删除评论失败: {}", e.getMessage());
            if (e.getMessage().contains("不存在")) {
                return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("删除评论失败", e);
            return new ResponseEntity<>(Result.error(500, "删除评论失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}