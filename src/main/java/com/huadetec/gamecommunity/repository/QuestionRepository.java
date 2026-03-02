package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 问答数据访问接口
 * 提供对question表的CRUD操作和自定义查询方法
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 根据游戏ID查找问答列表，按创建时间倒序排列
    List<Question> findByGameIdOrderByCreateTimeDesc(Long gameId);

    // 根据用户ID查找问答列表，按创建时间倒序排列
    List<Question> findByAuthorIdOrderByCreateTimeDesc(Long userId);

    // 搜索问答（按标题或内容包含关键词）
    List<Question> findByTitleContainingOrContentContainingOrderByCreateTimeDesc(String titleKeyword, String contentKeyword);
}