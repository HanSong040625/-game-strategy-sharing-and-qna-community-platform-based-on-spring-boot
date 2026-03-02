package com.huadetec.gamecommunity.service.impl;

import com.huadetec.gamecommunity.entity.Answer;
import com.huadetec.gamecommunity.repository.AnswerRepository;
import com.huadetec.gamecommunity.service.AnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * 回答服务实现类
 * 实现回答相关的业务逻辑
 */
@Service
public class AnswerServiceImpl implements AnswerService {

    private static final Logger logger = LoggerFactory.getLogger(AnswerServiceImpl.class);

    @Autowired
    private AnswerRepository answerRepository;
    
    @Autowired
    private com.huadetec.gamecommunity.service.QuestionService questionService;

    @Override
    public Answer createAnswer(Answer answer) {
        logger.info("创建新回答");
        // 参数校验
        Assert.hasText(answer.getContent(), "回答内容不能为空");
        Assert.notNull(answer.getAuthor(), "回答作者不能为空");
        Assert.notNull(answer.getQuestion(), "回答所属问题不能为空");
        
        // 设置默认值
        if (answer.getLikes() == null) {
            answer.setLikes(0);
        }
        if (answer.getDislikes() == null) {
            answer.setDislikes(0);
        }
        if (answer.getAccepted() == null) {
            answer.setAccepted(false);
        }
        
        // 保存回答
        Answer savedAnswer = answerRepository.save(answer);
        
        // 更新问题的回答数
        if (answer.getQuestion() != null && answer.getQuestion().getId() != null) {
            try {
                // 获取问题并更新回答数
                Optional<com.huadetec.gamecommunity.entity.Question> questionOptional = questionService.getQuestionById(answer.getQuestion().getId());
                if (questionOptional.isPresent()) {
                    com.huadetec.gamecommunity.entity.Question question = questionOptional.get();
                    // 获取当前问题的回答数并加1
                    Integer currentAnswerCount = question.getAnswerCount() != null ? question.getAnswerCount() : 0;
                    question.setAnswerCount(currentAnswerCount + 1);
                    questionService.updateQuestion(question);
                    logger.info("更新问题回答数成功，问题ID: {}，当前回答数: {}", answer.getQuestion().getId(), currentAnswerCount + 1);
                }
            } catch (Exception e) {
                logger.error("更新问题回答数失败，问题ID: {}", answer.getQuestion().getId(), e);
            }
        }
        
        return savedAnswer;
    }

    @Override
    public Answer updateAnswer(Answer answer) {
        logger.info("更新回答，ID：{}", answer.getId());
        // 参数校验
        Assert.notNull(answer.getId(), "回答ID不能为空");
        Assert.hasText(answer.getContent(), "回答内容不能为空");
        
        // 验证回答是否存在
        Optional<Answer> existingAnswer = answerRepository.findById(answer.getId());
        if (!existingAnswer.isPresent()) {
            throw new RuntimeException("回答不存在：" + answer.getId());
        }
        
        // 保存更新后的回答
        return answerRepository.save(answer);
    }

    @Override
    public void deleteAnswer(Long id) {
        logger.info("删除回答，ID：{}", id);
        // 验证回答是否存在
        if (!answerRepository.existsById(id)) {
            throw new RuntimeException("回答不存在：" + id);
        }
        // 删除回答
        answerRepository.deleteById(id);
    }

    @Override
    public Optional<Answer> getAnswerById(Long id) {
        logger.info("根据ID查找回答：{}", id);
        return answerRepository.findById(id);
    }

    @Override
    public List<Answer> getAnswersByQuestionId(Long questionId) {
        logger.info("根据问题ID获取回答列表：{}", questionId);
        return answerRepository.findByQuestionIdOrderByIsAcceptedDescCreateTimeAsc(questionId);
    }

    @Override
    public List<Answer> getAnswersByUserId(Long userId) {
        logger.info("根据用户ID获取回答列表：{}", userId);
        return answerRepository.findByAuthorIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public void voteAnswer(Long answerId, boolean isLike) {
        logger.info("为回答点赞/踩，ID：{}，操作：{}", answerId, isLike ? "点赞" : "踩");
        Optional<Answer> answerOptional = answerRepository.findById(answerId);
        if (answerOptional.isPresent()) {
            Answer answer = answerOptional.get();
            if (isLike) {
                answer.setLikes(answer.getLikes() + 1);
            } else {
                answer.setDislikes(answer.getDislikes() + 1);
            }
            answerRepository.save(answer);
        }
    }

    @Override
    public void acceptAnswer(Long answerId, Long questionId) {
        logger.info("采纳回答，回答ID：{}，问题ID：{}", answerId, questionId);
        
        // 先将该问题下的所有回答设置为未采纳
        List<Answer> answers = answerRepository.findByQuestionIdAndIsAcceptedTrue(questionId);
        for (Answer answer : answers) {
            answer.setAccepted(false);
            answerRepository.save(answer);
        }
        
        // 再将指定回答设置为采纳
        Optional<Answer> answerOptional = answerRepository.findById(answerId);
        if (answerOptional.isPresent()) {
            Answer answer = answerOptional.get();
            answer.setAccepted(true);
            answerRepository.save(answer);
        } else {
            throw new RuntimeException("回答不存在：" + answerId);
        }
    }
}