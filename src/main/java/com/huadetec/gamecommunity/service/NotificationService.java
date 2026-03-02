//创建Service接口和实现类
package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.Notification;
import com.huadetec.gamecommunity.entity.User;
import java.util.List;

public interface NotificationService {

    // 创建并发送通知
    Notification createNotification(User recipient, User sender, String content, String type, Long relatedId);
    
    // 创建并发送通知（支持管理员用户）
    Notification createNotificationWithSenderInfo(User recipient, Long senderId, String senderType, String content, String type, Long relatedId);

    // 获取用户未读通知
    List<Notification> getUnreadNotifications(User user);

    // 标记通知为已读
    void markAsRead(Long notificationId);

    // 获取用户所有通知
    List<Notification> getAllNotifications(User user);
    
    // 批量标记通知为已读（优化版本）
    void markAllAsRead(User user);
    
    // 删除单条通知
    void deleteNotification(Long notificationId, User user);
    
    // 删除多条通知
    void deleteNotifications(List<Long> notificationIds, User user);
    
    // 删除所有通知
    void deleteAllNotifications(User user);
}