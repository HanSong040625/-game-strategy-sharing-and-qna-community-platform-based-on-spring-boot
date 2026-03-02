//创建 NotificationRepository 接口来操作数据库
package com.huadetec.gamecommunity.repository;
import com.huadetec.gamecommunity.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.huadetec.gamecommunity.entity.User;
import java.util.List;
   

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>{
    // 根据接收人查找未读通知
    List<Notification> findByRecipientAndIsReadFalse(User recipient);

    // 根据接收人查找所有通知（可用于分页加载历史通知）
    List<Notification> findByRecipientOrderByCreateTimeDesc(User recipient);
    
    // 批量删除通知（继承自JpaRepository，但为了明确列出接口功能而添加注释）
    // void deleteAllById(Iterable<Long> ids);
    
    // 查找用户的多个通知
    List<Notification> findAllByIdInAndRecipient(Iterable<Long> ids, User recipient);
    
    // 根据关联ID和类型删除通知（用于清理与特定实体相关的通知）
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.relatedId = :relatedId AND n.type = :type")
    void deleteByRelatedIdAndType(@Param("relatedId") Long relatedId, @Param("type") String type);
    
    // 根据关联ID删除所有相关通知（用于清理与特定实体相关的所有通知）
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.relatedId = :relatedId")
    void deleteByRelatedId(@Param("relatedId") Long relatedId);
  
}

