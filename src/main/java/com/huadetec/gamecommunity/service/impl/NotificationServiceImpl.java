//实现NotificationService中的所有方法
package com.huadetec.gamecommunity.service.impl;
import com.huadetec.gamecommunity.entity.Notification;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.repository.NotificationRepository;
import com.huadetec.gamecommunity.service.NotificationService;
import com.huadetec.gamecommunity.controller.WebSocketNotificationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service // 注解：标识当前类是Spring管理的"服务层组件" 是将当前类注册到 Spring 容器中，使其成为可被自动注入的 Bean
//这个注解就是在说："Spring，请把这类变成 Bean 管理起来"
public class NotificationServiceImpl implements NotificationService{//实现接口NotificationService，实现所有抽象方法
    @Autowired //自动从Spring容器中注入NotificationRepository类型的实例
    //Spring 把NotificationRepository的实例，通过@Autowired注解自动给到NotificationServiceImpl的notificationRepository变量里 —— 这就是 "注入"。
    // ← Spring 自动将 NotificationRepository Bean 注入到这里
    private NotificationRepository notificationRepository;
    
    @Autowired
    private WebSocketNotificationController webSocketNotificationController;
    
    @Override
    public Notification createNotification(User recipient, User sender, String content, String type, Long relatedId) {
        Notification notification = new Notification();
// 实例化通知实体类
        notification.setRecipient(recipient);// 设置接收者
        // 设置发送者信息
        if (sender != null) {
            notification.setSenderId(sender.getId());
            notification.setSenderType("user"); // 普通用户
        }
        notification.setContent(content);// 设置通知内容
        notification.setType(type);// 设置通知类型
        notification.setRelatedId(relatedId);// 设置关联实体id
        notification.setIsRead(false);
        notification.setCreateTime(new Date());
        
        // 保存通知到数据库
        Notification savedNotification = notificationRepository.save(notification);
        
        // 发送WebSocket实时通知
        try {
            if (recipient != null && recipient.getId() != null && recipient.getId() > 0) {
                webSocketNotificationController.sendNotificationToUser(savedNotification, recipient.getId());
            }
        } catch (Exception e) {
            // WebSocket发送失败不影响主流程，记录日志即可
            System.err.println("WebSocket通知发送失败: " + e.getMessage());
        }
        
        return savedNotification;
    }
    
    @Override
    public Notification createNotificationWithSenderInfo(User recipient, Long senderId, String senderType, String content, String type, Long relatedId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);// 设置接收者
        notification.setSenderId(senderId);// 设置发送者ID
        notification.setSenderType(senderType);// 设置发送者类型
        notification.setContent(content);// 设置通知内容
        notification.setType(type);// 设置通知类型
        notification.setRelatedId(relatedId);// 设置关联实体id
        notification.setIsRead(false);
        notification.setCreateTime(new Date());
        
        // 保存通知到数据库
        Notification savedNotification = notificationRepository.save(notification);
        
        // 发送WebSocket实时通知
        try {
            if (recipient != null && recipient.getId() != null && recipient.getId() > 0) {
                webSocketNotificationController.sendNotificationToUser(savedNotification, recipient.getId());
            }
        } catch (Exception e) {
            // WebSocket发送失败不影响主流程，记录日志即可
            System.err.println("WebSocket通知发送失败: " + e.getMessage());
        }
        
        return savedNotification;
    }
    
    @Override
    public List<Notification> getUnreadNotifications(User user) {
        // 检查是否为管理员用户（ID为-1表示管理员）
        if (user.getId() != null && user.getId() == -1L) {
            // 管理员用户不应该有通知，返回空列表
            return new ArrayList<>();
        }
        return notificationRepository.findByRecipientAndIsReadFalse(user);
    }
    
    @Override
    public void markAsRead(Long notificationId) {
         // 1. 根据ID查询通知，若不存在则抛出异常（默认NoSuchElementException）
      
          Notification notification = notificationRepository.findById(notificationId).orElseThrow() ;
             // 2. 设置通知为已读
          notification.setIsRead(true);
          // 3. 保存更新到数据库
          notificationRepository.save(notification);
    }
    
    @Override
    // getAllNotifications获取用户所有通知
    public List<Notification> getAllNotifications(User user){ // 参数：要查询的用户（接收者)
        // 检查是否为管理员用户（ID为-1表示管理员）
        if (user.getId() != null && user.getId() == -1L) {
            // 管理员用户不应该有通知，返回空列表
            return new ArrayList<>();
        }
        // 调用Repository的方法，查询"接收者为user"的所有通知，并按"创建时间"倒序排列（最新的在前）
        return notificationRepository.findByRecipientOrderByCreateTimeDesc(user) ;
    }
    
    @Override
    @Transactional
    public void markAllAsRead(User user) {
        // 检查是否为管理员用户（ID为-1表示管理员）
        if (user.getId() != null && user.getId() == -1L) {
            // 管理员用户不应该有通知，直接返回
            return;
        }
        List<Notification> unreadNotifications = notificationRepository.findByRecipientAndIsReadFalse(user);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
    
    @Override
    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null && notification.getRecipient().equals(user)) {
            notificationRepository.delete(notification);
        }
    }
    
    @Override
    @Transactional
    public void deleteNotifications(List<Long> notificationIds, User user) {
        // 使用更高效的查询方法，直接过滤出属于当前用户的通知
        List<Notification> notifications = notificationRepository.findAllByIdInAndRecipient(notificationIds, user);
        if (!notifications.isEmpty()) {
            notificationRepository.deleteAll(notifications);
        }
    }
    
    @Override
    @Transactional
    public void deleteAllNotifications(User user) {
        List<Notification> userNotifications = notificationRepository.findByRecipientOrderByCreateTimeDesc(user);
        notificationRepository.deleteAll(userNotifications);
    }
}
