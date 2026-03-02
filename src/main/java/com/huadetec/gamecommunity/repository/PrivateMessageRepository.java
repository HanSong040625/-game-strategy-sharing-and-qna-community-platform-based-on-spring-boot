package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    /**
     * 根据发送者和接收者查询消息列表（按时间倒序）
     */
    @Query("SELECT pm FROM PrivateMessage pm WHERE " +
           "((pm.senderId = :senderId AND pm.receiverId = :receiverId) OR " +
           "(pm.senderId = :receiverId AND pm.receiverId = :senderId)) " +
           "ORDER BY pm.sendTime ASC")
    List<PrivateMessage> findConversationMessages(@Param("senderId") Long senderId, 
                                                 @Param("receiverId") Long receiverId);

    /**
     * 根据发送者和接收者查询消息列表（分页，按时间倒序）
     */
    @Query("SELECT pm FROM PrivateMessage pm WHERE " +
           "((pm.senderId = :senderId AND pm.receiverId = :receiverId) OR " +
           "(pm.senderId = :receiverId AND pm.receiverId = :senderId)) " +
           "ORDER BY pm.sendTime DESC")
    List<PrivateMessage> findConversationMessagesWithPagination(@Param("senderId") Long senderId, 
                                                               @Param("receiverId") Long receiverId);

    /**
     * 查询用户的所有对话列表（返回每个对话的最新一条消息）
     */
    @Query("SELECT pm FROM PrivateMessage pm WHERE pm.id IN (" +
           "SELECT MAX(pm2.id) FROM PrivateMessage pm2 WHERE " +
           "(pm2.senderId = :userId OR pm2.receiverId = :userId) " +
           "GROUP BY LEAST(pm2.senderId, pm2.receiverId), GREATEST(pm2.senderId, pm2.receiverId)" +
           ") ORDER BY pm.sendTime DESC")
    List<PrivateMessage> findUserConversations(@Param("userId") Long userId);

    /**
     * 查询用户发送的消息数量
     */
    @Query("SELECT COUNT(pm) FROM PrivateMessage pm WHERE " +
           "pm.senderId = :userId")
    Long countSentMessages(@Param("userId") Long userId);

    /**
     * 查询用户接收的消息数量
     */
    @Query("SELECT COUNT(pm) FROM PrivateMessage pm WHERE " +
           "pm.receiverId = :userId")
    Long countReceivedMessages(@Param("userId") Long userId);
}