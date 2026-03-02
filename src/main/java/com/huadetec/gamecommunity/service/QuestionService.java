package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.Question;
import java.util.List;
import java.util.Optional;

/**
 * 问答服务接口
 * 定义问答相关的业务逻辑操作
 */
public interface QuestionService {

    /**
     * 创建新问题
     * @param question 问题对象
     * @return 创建后的问题对象
     */
    Question createQuestion(Question question);

    /**
     * 更新问题
     * @param question 问题对象
     * @return 更新后的问题对象
     */
    Question updateQuestion(Question question);

    /**
     * 删除问题
     * @param id 问题ID
     */
    void deleteQuestion(Long id);

    /**
     * 根据ID查找问题
     * @param id 问题ID
     * @return 问题对象
     */
    Optional<Question> getQuestionById(Long id);

    /**
     * 根据游戏ID获取问题列表，如果gameId为null则返回所有问题
     * @param gameId 游戏ID（可选）
     * @return 问题列表
     */
    List<Question> getQuestionsByGameId(Long gameId);

    /**
     * 根据用户ID获取问题列表
     * @param userId 用户ID
     * @return 问题列表
     */
    List<Question> getQuestionsByUserId(Long userId);

    /**
     * 搜索问题
     * @param keyword 搜索关键词
     * @return 问题列表
     */
    List<Question> searchQuestions(String keyword);

    /**
     * 增加问题浏览量
     * @param questionId 问题ID
     */
    void incrementViewCount(Long questionId);

    /**
     * 为问题点赞/踩
     * @param questionId 问题ID
     * @param isLike 是否点赞（true为点赞，false为踩）
     */
    void voteQuestion(Long questionId, boolean isLike);
}