package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.Comment;
import com.huadetec.gamecommunity.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * 评论服务接口
 * 定义评论相关的业务逻辑操作
 */
public interface CommentService {

    // 根据ID获取评论
    Optional<Comment> getCommentById(Long id);

    // 根据攻略ID获取评论列表
    List<Comment> getCommentsByGuideId(Long guideId);

    // 创建评论
    Comment createComment(Comment comment);

    // 更新评论（如果需要）
    Comment updateComment(Comment comment);

    // 删除评论
    void deleteComment(Long id);

    // 为评论点赞
    void likeComment(Long commentId);
    
    // 为评论点赞（带用户参数，用于发送通知）
    void likeComment(Long commentId, User user);

    // 获取用户的评论列表
    List<Comment> getCommentsByUserId(Long userId);
}