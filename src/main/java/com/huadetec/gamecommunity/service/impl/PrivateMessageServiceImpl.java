package com.huadetec.gamecommunity.service.impl;

import com.huadetec.gamecommunity.controller.WebSocketNotificationController;
import com.huadetec.gamecommunity.entity.PrivateMessage;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.repository.PrivateMessageRepository;
import com.huadetec.gamecommunity.repository.UserRepository;
import com.huadetec.gamecommunity.service.PrivateMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private static final Logger logger = LoggerFactory.getLogger(PrivateMessageServiceImpl.class);
    
    @Autowired
    private PrivateMessageRepository privateMessageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WebSocketNotificationController webSocketNotificationController;

    @Override
    public PrivateMessage sendMessage(Long senderId, Long receiverId, String content) {
        logger.info("开始发送私聊消息: 发送者ID={}, 接收者ID={}, 内容长度={}", 
                   senderId, receiverId, content.length());
        
        // 参数校验
        Assert.notNull(senderId, "发送者ID不能为空");
        Assert.notNull(receiverId, "接收者ID不能为空");
        Assert.hasText(content, "消息内容不能为空");
        
        // 检查发送者和接收者是否存在
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("发送者不存在"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("接收者不存在"));
        
        // 检查不能为自己发消息
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能为自己发送消息");
        }
        
        // 创建消息对象
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setSendTime(new Date());
        
        // 保存消息
        PrivateMessage savedMessage = privateMessageRepository.save(message);
        
        logger.info("私聊消息发送成功: 消息ID={}, 发送者={}, 接收者={}", 
                   savedMessage.getId(), sender.getUsername(), receiver.getUsername());
        
        // 通过WebSocket推送实时消息
        try {
            logger.info("开始推送WebSocket实时消息: 接收者ID={}, 消息ID={}", receiverId, savedMessage.getId());
            webSocketNotificationController.sendPrivateMessageNotification(receiverId, savedMessage);
            logger.info("WebSocket实时消息推送成功");
        } catch (Exception e) {
            logger.error("WebSocket实时消息推送失败: {}", e.getMessage(), e);
            // WebSocket推送失败不影响主流程，继续返回消息
        }
        
        return savedMessage;
    }

    @Override
    public List<PrivateMessage> getConversationMessages(Long currentUserId, Long targetUserId) {
        logger.info("获取对话消息: 当前用户ID={}, 目标用户ID={}", currentUserId, targetUserId);
        
        // 参数校验
        Assert.notNull(currentUserId, "当前用户ID不能为空");
        Assert.notNull(targetUserId, "目标用户ID不能为空");
        
        // 检查用户是否存在
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("目标用户不存在"));
        
        // 获取对话消息
        List<PrivateMessage> messages = privateMessageRepository
                .findConversationMessages(currentUserId, targetUserId);
        
        logger.info("获取到对话消息数量: {}", messages.size());
        
        return messages;
    }

    @Override
    public List<Map<String, Object>> getUserConversations(Long userId) {
        logger.info("获取用户对话列表: 用户ID={}", userId);
        
        // 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        
        // 检查用户是否存在
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 获取对话列表
        List<PrivateMessage> latestMessages = privateMessageRepository
                .findUserConversations(userId);
        
        List<Map<String, Object>> conversations = new ArrayList<>();
        
        for (PrivateMessage message : latestMessages) {
            Map<String, Object> conversation = new HashMap<>();
            
            // 确定对话的另一方
            Long otherUserId = message.getSenderId().equals(userId) ? 
                    message.getReceiverId() : message.getSenderId();
            
            // 获取对方用户信息
            User otherUser = userRepository.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("对话用户不存在"));
            
            conversation.put("otherUser", otherUser);
            conversation.put("latestMessage", message);
            conversation.put("lastMessageTime", message.getSendTime());
            
            conversations.add(conversation);
        }
        
        logger.info("获取到用户对话数量: {}", conversations.size());
        
        return conversations;
    }

    @Override
    public PrivateMessage getMessageById(Long messageId) {
        logger.info("获取消息详情: 消息ID={}", messageId);
        
        // 参数校验
        Assert.notNull(messageId, "消息ID不能为空");
        
        PrivateMessage message = privateMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        
        logger.info("获取到消息详情: 消息ID={}, 发送者ID={}, 接收者ID={}", 
                   message.getId(), message.getSenderId(), message.getReceiverId());
        
        return message;
    }

    @Override
    public Map<String, Object> getUserMessageStats(Long userId) {
        logger.info("获取用户消息统计: 用户ID={}", userId);
        
        // 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        
        // 检查用户是否存在
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取发送消息数量
        Long sentCount = privateMessageRepository.countSentMessages(userId);
        // 获取接收消息数量
        Long receivedCount = privateMessageRepository.countReceivedMessages(userId);
        
        stats.put("sentCount", sentCount);
        stats.put("receivedCount", receivedCount);
        stats.put("totalCount", sentCount + receivedCount);
        
        logger.info("用户消息统计: 发送={}, 接收={}", sentCount, receivedCount);
        
        return stats;
    }
}