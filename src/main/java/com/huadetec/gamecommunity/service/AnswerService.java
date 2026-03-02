package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.Answer;
import java.util.List;
import java.util.Optional;

/**
 * 回答服务接口
 * 定义回答相关的业务逻辑操作
 */
public interface AnswerService {

    /**
     * 创建新回答
     * @param answer 回答对象
     * @return 创建后的回答对象
     */
    Answer createAnswer(Answer answer);

    /**
     * 更新回答
     * @param answer 回答对象
     * @return 更新后的回答对象
     */
    Answer updateAnswer(Answer answer);

    /**
     * 删除回答
     * @param id 回答ID
     */
    void deleteAnswer(Long id);

    /**
     * 根据ID查找回答
     * @param id 回答ID
     * @return 回答对象
     */
    Optional<Answer> getAnswerById(Long id);

    /**
     * 根据问题ID获取回答列表
     * @param questionId 问题ID
     * @return 回答列表
     */
    List<Answer> getAnswersByQuestionId(Long questionId);

    /**
     * 根据用户ID获取回答列表
     * @param userId 用户ID
     * @return 回答列表
     */
    List<Answer> getAnswersByUserId(Long userId);

    /**
     * 为回答点赞/踩
     * @param answerId 回答ID
     * @param isLike 是否点赞（true为点赞，false为踩）
     */
    void voteAnswer(Long answerId, boolean isLike);

    /**
     * 采纳回答
     * @param answerId 回答ID
     * @param questionId 问题ID
     */
    void acceptAnswer(Long answerId, Long questionId);
}