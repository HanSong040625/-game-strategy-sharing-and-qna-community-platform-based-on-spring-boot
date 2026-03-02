package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.Notification;
import com.huadetec.gamecommunity.entity.PrivateMessage;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.service.NotificationService;
import com.huadetec.gamecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket通知控制器
 * 处理实时通知的发送和接收
 */
@Controller
public class WebSocketNotificationController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    /**
     * 发送实时通知给指定用户
     * @param notification 通知对象
     * @param recipientId 接收者ID
     */
    public void sendNotificationToUser(Notification notification, Long recipientId) {
        if (recipientId != null && recipientId > 0) {
            // 构建通知消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", "notification");
            message.put("notification", notification);
            message.put("timestamp", System.currentTimeMillis());
            
            // 发送给特定用户
            messagingTemplate.convertAndSendToUser(
                recipientId.toString(), 
                "/topic/notifications", 
                message
            );
        }
    }

    /**
     * 处理用户订阅通知频道
     */
    @MessageMapping("/notifications/subscribe")
    @SendTo("/topic/notifications")
    public Map<String, Object> subscribeNotifications(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "subscription");
        response.put("status", "subscribed");
        response.put("timestamp", System.currentTimeMillis());
        
        if (principal != null) {
            response.put("username", principal.getName());
        }
        
        return response;
    }

    /**
     * 处理标记通知为已读的WebSocket消息
     */
    @MessageMapping("/notifications/mark-read")
    public void markNotificationAsRead(@RequestBody Map<String, Object> payload, Principal principal) {
        try {
            Long notificationId = Long.valueOf(payload.get("notificationId").toString());
            
            // 标记通知为已读
            notificationService.markAsRead(notificationId);
            
            // 发送确认消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "mark-read");
            response.put("notificationId", notificationId);
            response.put("status", "success");
            response.put("timestamp", System.currentTimeMillis());
            
            if (principal != null) {
                messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/topic/notifications",
                    response
                );
            }
        } catch (Exception e) {
            // 发送错误消息
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "mark-read");
            errorResponse.put("status", "error");
            errorResponse.put("message", "标记通知为已读失败");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            if (principal != null) {
                messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/topic/notifications",
                    errorResponse
                );
            }
        }
    }

    /**
     * 发送私聊消息实时通知给接收者
     * @param recipientId 接收者ID
     * @param privateMessage 私聊消息对象
     */
    public void sendPrivateMessageNotification(Long recipientId, PrivateMessage privateMessage) {
        if (recipientId != null && recipientId > 0 && privateMessage != null) {
            // 构建私聊消息通知 - 只发送必要的字段，避免序列化问题
            Map<String, Object> message = new HashMap<>();
            message.put("type", "private_message");
            message.put("messageId", privateMessage.getId());
            message.put("senderId", privateMessage.getSenderId());
            message.put("receiverId", privateMessage.getReceiverId());
            message.put("content", privateMessage.getContent());
            message.put("messageType", privateMessage.getMessageType());
            message.put("sendTime", privateMessage.getSendTime());
            message.put("timestamp", System.currentTimeMillis());
            
            // 发送给特定用户
            messagingTemplate.convertAndSendToUser(
                recipientId.toString(), 
                "/topic/private-messages", 
                message
            );
            
            // 同时发送给发送者（用于实时更新自己的界面）
            messagingTemplate.convertAndSendToUser(
                privateMessage.getSenderId().toString(), 
                "/topic/private-messages", 
                message
            );
        }
    }
}