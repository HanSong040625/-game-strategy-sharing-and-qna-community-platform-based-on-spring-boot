package com.huadetec.gamecommunity.controller;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.entity.Notification;
import com.huadetec.gamecommunity.service.NotificationService;
import com.huadetec.gamecommunity.service.AdminService;
import com.huadetec.gamecommunity.service.UserService;
import com.huadetec.gamecommunity.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通知控制器 - 处理用户通知相关的HTTP请求
 * 负责获取未读通知、所有通知、标记通知为已读等功能
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    // 使用SLF4J日志记录器（Spring Boot推荐的日志框架）
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AdminService adminService;

    /**
     * 获取当前登录用户
     * @return 当前登录用户对象
     * @throws RuntimeException 当用户不存在或未登录时抛出异常
     */
    /**
     * 获取当前登录用户
     * 改进版：使用SLF4J日志，提供更详细的调试信息和更好的错误处理
     * @return 当前登录用户对象
     * @throws RuntimeException 当用户不存在或未登录时抛出异常
     */
    private User getCurrentUser() {
        logger.debug("开始获取当前登录用户信息");
        
        // 从Spring Security上下文获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        logger.debug("认证信息: {}", authentication != null ? "存在" : "不存在");
        
        // 检查用户是否已认证
        if (authentication == null) {
            logger.error("认证信息为null，用户未登录");
            throw new RuntimeException("用户未登录");
        }
        
        logger.debug("用户已认证: {}", authentication.isAuthenticated());
        
        if (!authentication.isAuthenticated()) {
            logger.error("用户未完成认证");
            throw new RuntimeException("用户未登录");
        }
        
        // 获取认证主体
        Object principal = authentication.getPrincipal();
        logger.debug("认证主体类型: {}", principal.getClass().getName());
        
        // 检查是否为匿名用户
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            logger.error("检测到匿名用户访问");
            throw new RuntimeException("用户未登录");
        }
        
        // 获取用户名
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
            logger.debug("从UserDetails获取用户名: {}", username);
        } else {
            username = principal.toString();
            logger.debug("从其他类型主体获取用户名: {}", username);
        }
        
        logger.debug("准备根据用户名[{}]查询用户对象", username);
        
        // 首先尝试从用户表中查找用户
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            logger.debug("成功从用户表获取用户对象 - ID: {}, 用户名: {}", user.getId(), user.getUsername());
            return user;
        }
        
        // 如果用户表中不存在，检查是否是管理员用户
        logger.debug("用户表中未找到用户，检查是否是管理员用户: {}", username);
        
        // 检查是否为管理员用户
        if (adminService != null && adminService.isAdminUser(username)) {
            logger.debug("检测到管理员用户: {}", username);
            // 管理员用户不应该使用通知功能，返回空的通知列表
        // 创建一个临时的User对象，避免返回401错误
        User adminUser = new User();
        adminUser.setId(-1L); // 使用特殊ID标识管理员用户
        adminUser.setUsername(username);
        adminUser.setIsAdmin(1); // 标记为管理员，使用Integer类型
        logger.debug("为管理员用户创建临时用户对象，避免通知功能异常");
        return adminUser;
        }
        
        // 如果用户表中不存在且不是管理员，但Spring Security认证成功
        // 说明用户是通过Spring Security认证的，可能是数据库中没有对应记录
        throw new RuntimeException("用户不存在");
    }
    
    /**
     * 获取当前用户的未读通知
     * @return 包含未读通知列表和数量的响应对象
     */
    @GetMapping("/unread")
    public ResponseEntity<Result> getUnreadNotifications() {
        logger.debug("接收到获取未读通知请求");
        
        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser();
            logger.debug("为用户[ID: {}, Username: {}]获取未读通知", currentUser.getId(), currentUser.getUsername());
            
            // 调用服务层获取未读通知
            List<Notification> unreadNotifications = notificationService.getUnreadNotifications(currentUser);
            logger.debug("获取未读通知数量: {}", unreadNotifications != null ? unreadNotifications.size() : 0);
            
            // 构建成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("data", unreadNotifications);
            response.put("count", unreadNotifications != null ? unreadNotifications.size() : 0);
            
            logger.debug("未读通知请求处理完成，返回数据");
            return new ResponseEntity<Result>(Result.success(response), HttpStatus.OK);
        } catch (RuntimeException e) {
            // 处理业务逻辑异常（如用户不存在、未登录等）
            logger.error("业务逻辑错误: {}", e.getMessage());
            logger.debug("业务异常详情:", e); // debug级别记录堆栈，避免生产环境日志过大
            
            // 针对用户不存在或未登录的情况，确保返回401状态码
            if (e.getMessage().contains("用户不存在") || e.getMessage().contains("用户未登录")) {
                return new ResponseEntity<Result>(Result.error(401, e.getMessage()), HttpStatus.UNAUTHORIZED);
            } else {
                // 其他业务异常
                return new ResponseEntity<Result>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            // 处理其他未预期的异常
            logger.error("获取未读通知时发生系统错误:", e); // 使用SLF4J的正确方式记录异常
            
            // 构建系统错误响应
            return new ResponseEntity<Result>(Result.error(500, "系统内部错误，请稍后重试"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取当前用户的所有通知
     * @return 包含所有通知列表的响应对象
     */
    @GetMapping("/all")
    public ResponseEntity<Result> getAllNotifications() {
        logger.debug("接收到获取所有通知请求");
        
        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser();
            logger.debug("为用户[ID: {}, Username: {}]获取所有通知", currentUser.getId(), currentUser.getUsername());
            
            // 调用服务层获取所有通知
            List<Notification> allNotifications = notificationService.getAllNotifications(currentUser);
            logger.debug("获取所有通知数量: {}", allNotifications != null ? allNotifications.size() : 0);
            
            logger.debug("所有通知请求处理完成，返回数据");
            return new ResponseEntity<Result>(Result.success(allNotifications), HttpStatus.OK);
        } catch (RuntimeException e) {
            // 处理业务逻辑异常（如用户不存在、未登录等）
            logger.error("业务逻辑错误: {}", e.getMessage());
            logger.debug("业务异常详情:", e); // debug级别记录堆栈，避免生产环境日志过大
            
            // 构建错误响应 - 根据不同异常返回更友好的信息
            return new ResponseEntity<Result>(Result.error(401, e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            // 处理其他未预期的异常
            logger.error("获取所有通知时发生系统错误:", e); // 使用SLF4J的正确方式记录异常
            
            // 构建系统错误响应
            return new ResponseEntity<Result>(Result.error(500, "系统内部错误，请稍后重试"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 标记单个通知为已读
     * @param id 通知ID
     * @return 操作结果响应对象
     */
    @PutMapping("/read/{id}")
    public ResponseEntity<Result> markAsRead(@PathVariable Long id) {
        logger.debug("接收到标记通知为已读请求，通知ID: {}", id);
        
        try {
            // 验证用户身份（确保用户已登录）
            User currentUser = getCurrentUser();
            logger.debug("用户[ID: {}, Username: {}]请求标记通知[ID: {}]为已读", 
                         currentUser.getId(), currentUser.getUsername(), id);
            
            // 调用服务层标记通知为已读
            notificationService.markAsRead(id);
            logger.debug("通知[ID: {}]标记为已读成功", id);
            
            logger.debug("标记通知已读请求处理完成");
            return new ResponseEntity<Result>(Result.success("标记通知为已读成功"), HttpStatus.OK);
        } catch (RuntimeException e) {
            // 处理业务逻辑异常（如用户不存在、未登录等）
            logger.error("业务逻辑错误: {}", e.getMessage());
            logger.debug("业务异常详情:", e); // debug级别记录堆栈，避免生产环境日志过大
            
            // 构建错误响应 - 根据不同异常返回更友好的信息
            return new ResponseEntity<Result>(Result.error(401, e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            // 处理其他未预期的异常
            logger.error("标记通知[ID: {}]为已读时发生系统错误:", id, e); // 使用SLF4J的正确方式记录异常
            
            // 构建系统错误响应
            return new ResponseEntity<Result>(Result.error(500, "系统内部错误，请稍后重试"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 标记所有通知为已读
     * @return 操作结果响应对象，包含标记已读的通知数量
     */
    @PutMapping("/read-all")
    public ResponseEntity<Result> markAllAsRead() {
        logger.debug("接收到标记所有通知为已读请求");
        
        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser();
            logger.debug("用户[ID: {}, Username: {}]请求标记所有通知为已读", 
                         currentUser.getId(), currentUser.getUsername());
            
            // 调用服务层方法标记所有通知为已读
            notificationService.markAllAsRead(currentUser);
            
            logger.debug("标记所有通知已读请求处理完成");
            return new ResponseEntity<Result>(Result.success("已将所有通知标记为已读"), HttpStatus.OK);
        } catch (RuntimeException e) {
            // 处理业务逻辑异常（如用户不存在、未登录等）
            logger.error("业务逻辑错误: {}", e.getMessage());
            logger.debug("业务异常详情:", e); // debug级别记录堆栈，避免生产环境日志过大
            
            // 针对用户不存在或未登录的情况，确保返回401状态码
            if (e.getMessage().contains("用户不存在") || e.getMessage().contains("用户未登录")) {
                return new ResponseEntity<Result>(Result.error(401, e.getMessage()), HttpStatus.UNAUTHORIZED);
            } else {
                // 其他业务异常
                return new ResponseEntity<Result>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            // 处理其他未预期的异常
            logger.error("标记所有通知为已读时发生系统错误:", e);
            
            // 构建系统错误响应
            return new ResponseEntity<Result>(Result.error(500, "系统内部错误，请稍后重试"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteNotification(@PathVariable("id") Long notificationId) {
        logger.debug("接收到删除通知请求，通知ID: {}", notificationId);
        
        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser();
            logger.debug("用户[ID: {}, Username: {}]请求删除通知[ID: {}}", 
                         currentUser.getId(), currentUser.getUsername(), notificationId);
            
            // 调用服务层方法删除通知
            notificationService.deleteNotification(notificationId, currentUser);
            
            logger.debug("删除通知请求处理完成");
            return new ResponseEntity<Result>(Result.success("通知删除成功"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("删除通知时发生错误: {}", e.getMessage());
            logger.debug("业务异常详情:", e);
            
            if (e.getMessage().contains("用户不存在") || e.getMessage().contains("用户未登录")) {
                return new ResponseEntity<Result>(Result.error(401, e.getMessage()), HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<Result>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
            }
        }
    }
    
    @DeleteMapping("/batch")
    public ResponseEntity<Result> deleteNotifications(@RequestBody List<Long> notificationIds) {
        logger.debug("接收到批量删除通知请求，通知ID数量: {}", notificationIds != null ? notificationIds.size() : 0);
        
        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser();
            logger.debug("用户[ID: {}, Username: {}]请求批量删除通知", 
                         currentUser.getId(), currentUser.getUsername());
            
            // 调用服务层方法批量删除通知
            notificationService.deleteNotifications(notificationIds, currentUser);
            
            logger.debug("批量删除通知请求处理完成");
            return new ResponseEntity<Result>(Result.success("通知批量删除成功"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("批量删除通知时发生错误: {}", e.getMessage());
            logger.debug("业务异常详情:", e);
            
            if (e.getMessage().contains("用户不存在") || e.getMessage().contains("用户未登录")) {
                return new ResponseEntity<Result>(Result.error(401, e.getMessage()), HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<Result>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
            }
        }
    }
    
    @DeleteMapping("/all")
    public ResponseEntity<Result> deleteAllNotifications() {
        logger.debug("接收到删除所有通知请求");
        
        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser();
            logger.debug("用户[ID: {}, Username: {}]请求删除所有通知", 
                         currentUser.getId(), currentUser.getUsername());
            
            // 调用服务层方法删除所有通知
            notificationService.deleteAllNotifications(currentUser);
            
            logger.debug("删除所有通知请求处理完成");
            return new ResponseEntity<Result>(Result.success("所有通知删除成功"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("删除所有通知时发生错误: {}", e.getMessage());
            logger.debug("业务异常详情:", e);
            
            if (e.getMessage().contains("用户不存在") || e.getMessage().contains("用户未登录")) {
                return new ResponseEntity<Result>(Result.error(401, e.getMessage()), HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<Result>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
            }
        }
    }
}


