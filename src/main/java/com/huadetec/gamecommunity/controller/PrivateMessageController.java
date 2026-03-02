package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.PrivateMessage;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.service.PrivateMessageService;
import com.huadetec.gamecommunity.service.UserService;
import com.huadetec.gamecommunity.controller.WebSocketNotificationController;
import com.huadetec.gamecommunity.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/private-messages")
public class PrivateMessageController {

    private static final Logger logger = LoggerFactory.getLogger(PrivateMessageController.class);
    
    @Autowired
    private PrivateMessageService privateMessageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private WebSocketNotificationController webSocketNotificationController;

    /**
     * 发送私聊消息
     */
    @PostMapping("/send")
    public ResponseEntity<Result> sendMessage(@RequestParam Long receiverId, 
                                                           @RequestParam String content) {
        logger.info("接收到发送私聊消息请求: 接收者ID={}, 内容长度={}", receiverId, content.length());
        
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal() instanceof String) {
                return new ResponseEntity<Result>(Result.error(401, "用户未登录"), HttpStatus.UNAUTHORIZED);
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 获取当前用户ID
            Optional<User> currentUserOpt = userService.findByUsername(username);
            if (!currentUserOpt.isPresent()) {
                return new ResponseEntity<Result>(Result.error(401, "用户不存在"), HttpStatus.UNAUTHORIZED);
            }
            
            Long senderId = currentUserOpt.get().getId();
            
            // 参数校验
            if (receiverId == null) {
                return new ResponseEntity<Result>(Result.error(400, "接收者ID不能为空"), HttpStatus.BAD_REQUEST);
            }
            
            if (content == null || content.trim().isEmpty()) {
                return new ResponseEntity<Result>(Result.error(400, "消息内容不能为空"), HttpStatus.BAD_REQUEST);
            }
            
            // 检查不能为自己发消息
            if (senderId.equals(receiverId)) {
                return new ResponseEntity<Result>(Result.error(400, "不能为自己发送消息"), HttpStatus.BAD_REQUEST);
            }
            
            // 发送消息
            PrivateMessage message = privateMessageService.sendMessage(senderId, receiverId, content);
            
            // 发送WebSocket通知给接收者
            webSocketNotificationController.sendPrivateMessageNotification(receiverId, message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", createMessageResponse(message));
            
            logger.info("私聊消息发送成功: 消息ID={}, 发送者={}, 接收者ID={}", 
                       message.getId(), username, receiverId);
            
            return new ResponseEntity<Result>(Result.success(response), HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("发送私聊消息失败: {}", e.getMessage(), e);
            return new ResponseEntity<Result>(Result.error(500, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取对话消息列表
     */
    @GetMapping("/conversation/{targetUserId}")
    public ResponseEntity<Map<String, Object>> getConversationMessages(@PathVariable Long targetUserId) {
        logger.info("接收到获取对话消息请求: 目标用户ID={}", targetUserId);
        
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal() instanceof String) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse(401, "用户未登录"));
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 获取当前用户ID
            Optional<User> currentUserOpt = userService.findByUsername(username);
            if (!currentUserOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse(401, "用户不存在"));
            }
            
            Long currentUserId = currentUserOpt.get().getId();
            
            // 参数校验
            if (targetUserId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(400, "目标用户ID不能为空"));
            }
            
            // 获取对话消息
            List<PrivateMessage> messages = privateMessageService
                    .getConversationMessages(currentUserId, targetUserId);
            
            Map<String, Object> response = createSuccessResponse("获取对话消息成功");
            response.put("messages", messages.stream()
                    .map(this::createMessageResponse)
                    .toArray());
            response.put("currentUserId", currentUserId);
            response.put("targetUserId", targetUserId);
            
            logger.info("获取对话消息成功: 消息数量={}, 当前用户={}, 目标用户ID={}", 
                       messages.size(), username, targetUserId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取对话消息失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(500, e.getMessage()));
        }
    }

    /**
     * 获取用户对话列表
     */
    @GetMapping("/conversations")
    public ResponseEntity<Map<String, Object>> getUserConversations() {
        logger.info("接收到获取用户对话列表请求");
        
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal() instanceof String) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse(401, "用户未登录"));
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 获取当前用户ID
            Optional<User> currentUserOpt = userService.findByUsername(username);
            if (!currentUserOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse(401, "用户不存在"));
            }
            
            Long currentUserId = currentUserOpt.get().getId();
            
            // 获取对话列表
            List<Map<String, Object>> conversations = privateMessageService
                    .getUserConversations(currentUserId);
            
            Map<String, Object> response = createSuccessResponse("获取对话列表成功");
            response.put("conversations", conversations);
            response.put("currentUserId", currentUserId);
            
            logger.info("获取用户对话列表成功: 对话数量={}, 用户={}", 
                       conversations.size(), username);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取用户对话列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(500, e.getMessage()));
        }
    }

    /**
     * 获取用户消息统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserMessageStats() {
        logger.info("接收到获取用户消息统计请求");
        
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal() instanceof String) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse(401, "用户未登录"));
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 获取当前用户ID
            Optional<User> currentUserOpt = userService.findByUsername(username);
            if (!currentUserOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse(401, "用户不存在"));
            }
            
            Long currentUserId = currentUserOpt.get().getId();
            
            // 获取消息统计
            Map<String, Object> stats = privateMessageService.getUserMessageStats(currentUserId);
            
            Map<String, Object> response = createSuccessResponse("获取消息统计成功");
            response.put("stats", stats);
            
            logger.info("获取用户消息统计成功: 用户={}", username);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取用户消息统计失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(500, e.getMessage()));
        }
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", new HashMap<>());
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("data", null);
        return response;
    }

    /**
     * 创建消息响应对象
     */
    private Map<String, Object> createMessageResponse(PrivateMessage message) {
        Map<String, Object> messageResponse = new HashMap<>();
        messageResponse.put("id", message.getId());
        messageResponse.put("senderId", message.getSenderId());
        messageResponse.put("receiverId", message.getReceiverId());
        messageResponse.put("content", message.getContent());
        messageResponse.put("sendTime", message.getSendTime());
        return messageResponse;
    }
}