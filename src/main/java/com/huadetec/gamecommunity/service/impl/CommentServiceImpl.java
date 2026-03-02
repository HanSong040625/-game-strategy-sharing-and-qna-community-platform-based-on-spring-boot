package com.huadetec.gamecommunity.service.impl;

import com.huadetec.gamecommunity.entity.Comment;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.repository.CommentRepository;
import com.huadetec.gamecommunity.service.CommentService;
import com.huadetec.gamecommunity.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 评论服务实现类
 * 实现评论相关的业务逻辑操作
 */
@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
    }

    /**
     * 根据ID获取评论
     * @param id 评论ID
     * @return 评论对象的Optional包装
     */
    @Override
    public Optional<Comment> getCommentById(Long id) {
        logger.info("获取评论详情，ID: {}", id);
        return commentRepository.findById(id);
    }

    /**
     * 根据攻略ID获取评论列表
     * @param guideId 攻略ID
     * @return 评论列表
     */
    @Override
    public List<Comment> getCommentsByGuideId(Long guideId) {
        logger.info("获取攻略的评论列表，攻略ID: {}", guideId);
        return commentRepository.findByGuideIdOrderByCreateTimeDesc(guideId);
    }

    /**
     * 创建评论
     * @param comment 评论对象
     * @return 创建后的评论对象
     */
    @Override
    public Comment createComment(Comment comment) {
        logger.info("创建新评论，攻略ID: {}, 用户ID: {}", 
                comment.getGuide().getId(), comment.getAuthor().getId());
        
        // 保存评论
        Comment savedComment = commentRepository.save(comment);
        
        // 发送评论通知给攻略作者
        sendCommentNotification(savedComment);
        
        return savedComment;
    }
    
    /**
     * 发送评论通知给攻略作者
     * @param comment 评论对象
     * 修改内容：在通知中添加评论的具体内容，方便用户直接查看
     */
    private void sendCommentNotification(Comment comment) {
        // 获取攻略作者作为通知接收者
        var guideAuthor = comment.getGuide().getAuthor();
        // 获取评论作者作为通知发送者
        var commentAuthor = comment.getAuthor();
        
        // 构建通知内容 - 包含评论具体内容
        String notificationContent = String.format("%s 评论了你的攻略《%s》：%s", 
                commentAuthor.getUsername(), comment.getGuide().getTitle(), comment.getContent());
        
        // 创建评论通知
        notificationService.createNotification(
                guideAuthor,           // 接收者：攻略作者
                commentAuthor,         // 发送者：评论作者
                notificationContent,   // 通知内容（包含评论内容）
                "comment",            // 通知类型：评论
                comment.getId()        // 相关ID：评论ID
        );
    }

    /**
     * 更新评论
     * @param comment 评论对象
     * @return 更新后的评论对象
     * @throws RuntimeException 如果评论不存在
     */
    @Override
    public Comment updateComment(Comment comment) {
        logger.info("更新评论，ID: {}", comment.getId());
        // 检查评论是否存在
        if (!commentRepository.existsById(comment.getId())) {
            logger.warn("评论不存在，ID: {}", comment.getId());
            throw new RuntimeException("评论不存在");
        }
        return commentRepository.save(comment);
    }

    /**
     * 删除评论
     * @param id 评论ID
     * @throws RuntimeException 如果评论不存在
     */
    @Override
    public void deleteComment(Long id) {
        logger.info("删除评论，ID: {}", id);
        if (!commentRepository.existsById(id)) {
            logger.warn("评论不存在，ID: {}", id);
            throw new RuntimeException("评论不存在");
        }
        commentRepository.deleteById(id);
    }

    /**
     * 为评论点赞（无用户参数版本，保留兼容性）
     * @param commentId 评论ID
     * @throws RuntimeException 如果评论不存在
     */
    @Override
    public void likeComment(Long commentId) {
        // 保留原有方法以保持兼容性，但实际业务逻辑由新方法处理
        // 这里简化处理，不发送通知
        logger.info("为评论点赞（无用户参数版本），评论ID: {}", commentId);
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            // 增加点赞数
            comment.setLikes(comment.getLikes() + 1);
            commentRepository.save(comment);
            logger.info("评论点赞成功，评论ID: {}, 当前点赞数: {}", 
                    commentId, comment.getLikes());
        } else {
            logger.warn("评论不存在，ID: {}", commentId);
            throw new RuntimeException("评论不存在");
        }
    }
    
    /**
     * 为评论点赞（带用户参数版本，用于发送通知）
     * @param commentId 评论ID
     * @param user 点赞的用户
     * @throws RuntimeException 如果评论不存在
     */
    @Override
    public void likeComment(Long commentId, User user) {
        logger.info("为评论点赞（带用户参数版本），评论ID: {}, 用户: {}", 
                commentId, user.getUsername());
        
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            // 增加点赞数
            comment.setLikes(comment.getLikes() + 1);
            commentRepository.save(comment);
            logger.info("评论点赞成功，评论ID: {}, 当前点赞数: {}", 
                    commentId, comment.getLikes());
            
            // 发送点赞通知给评论作者
            sendLikeNotification(comment, user);
        } else {
            logger.warn("评论不存在，ID: {}", commentId);
            throw new RuntimeException("评论不存在");
        }
    }
    
    /**
     * 发送点赞通知给评论作者
     * @param comment 评论对象
     * @param likeUser 点赞用户
     * 修改内容：在通知中添加被点赞的评论具体内容，方便用户直接查看
     */
    private void sendLikeNotification(Comment comment, User likeUser) {
        // 获取评论作者作为通知接收者
        User commentAuthor = comment.getAuthor();
        
        // 构建通知内容 - 包含被点赞的评论具体内容
        String notificationContent = String.format("%s 点赞了你的评论：%s", 
                likeUser.getUsername(), comment.getContent());
        
        // 创建点赞通知
        notificationService.createNotification(
                commentAuthor,         // 接收者：评论作者
                likeUser,              // 发送者：点赞用户
                notificationContent,   // 通知内容（包含被点赞的评论内容）
                "comment",            // 通知类型：评论（归类到评论我的）
                comment.getId()        // 相关ID：评论ID
        );
    }

    /**
     * 获取用户的评论列表
     * @param userId 用户ID
     * @return 用户的评论列表
     */
    @Override
    public List<Comment> getCommentsByUserId(Long userId) {
        logger.info("获取用户的评论列表，用户ID: {}", userId);
        return commentRepository.findByAuthorIdOrderByCreateTimeDesc(userId);
    }
}