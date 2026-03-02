package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论数据访问接口
 * 提供对comment表的CRUD操作和自定义查询方法
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 根据攻略ID查找评论列表，按创建时间倒序排列
    List<Comment> findByGuideIdOrderByCreateTimeDesc(Long guideId);

    // 根据用户ID查找评论列表，按创建时间倒序排列
    List<Comment> findByAuthorIdOrderByCreateTimeDesc(Long userId);

    // 根据攻略ID和用户ID查找评论（用于判断用户是否已评论过）
    List<Comment> findByGuideIdAndAuthorId(Long guideId, Long userId);

    // 根据攻略ID删除所有评论
    @Modifying
    @Query(value = "DELETE FROM comment WHERE guide_id = :guideId", nativeQuery = true)
    void deleteByGuideId(Long guideId);
}