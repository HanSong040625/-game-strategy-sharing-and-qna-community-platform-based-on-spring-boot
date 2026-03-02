package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 回答数据访问接口
 * 提供对answer表的CRUD操作和自定义查询方法
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // 根据问题ID查找回答列表，按创建时间升序排列
    List<Answer> findByQuestionIdOrderByCreateTimeAsc(Long questionId);

    // 根据问题ID查找回答列表，按是否被采纳（最佳答案）优先，然后按创建时间升序排列
    List<Answer> findByQuestionIdOrderByIsAcceptedDescCreateTimeAsc(Long questionId);

    // 根据用户ID查找回答列表，按创建时间倒序排列
    List<Answer> findByAuthorIdOrderByCreateTimeDesc(Long userId);

    // 根据问题ID和是否被采纳查找回答
    List<Answer> findByQuestionIdAndIsAcceptedTrue(Long questionId);
}