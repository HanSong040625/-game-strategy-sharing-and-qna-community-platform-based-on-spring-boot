package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.PrivateMessage;

import java.util.List;
import java.util.Map;

public interface PrivateMessageService {

    /**
     * 发送私聊消息
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 消息内容
     * @return 发送的消息
     */
    PrivateMessage sendMessage(Long senderId, Long receiverId, String content);

    /**
     * 获取对话消息列表
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 对话消息列表
     */
    List<PrivateMessage> getConversationMessages(Long currentUserId, Long targetUserId);

    /**
     * 获取用户对话列表
     * @param userId 用户ID
     * @return 对话列表，包含对方用户信息和最新消息
     */
    List<Map<String, Object>> getUserConversations(Long userId);

    /**
     * 根据消息ID获取消息详情
     * @param messageId 消息ID
     * @return 消息详情
     */
    PrivateMessage getMessageById(Long messageId);

    /**
     * 获取用户消息统计
     * @param userId 用户ID
     * @return 消息统计信息
     */
    Map<String, Object> getUserMessageStats(Long userId);
}