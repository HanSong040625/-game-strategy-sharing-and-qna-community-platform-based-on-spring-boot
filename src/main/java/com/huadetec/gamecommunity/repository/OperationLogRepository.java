package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志Repository接口
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {//Long：实体类主键类型
    
    /**
     * 根据管理员ID查找操作日志
     */
    List<OperationLog> findByAdminIdOrderByOperationTimeDesc(Long adminId);
    
    /**
     * 根据管理员ID分页查找操作日志
     */
    Page<OperationLog> findByAdminIdOrderByOperationTimeDesc(Long adminId, Pageable pageable);
    
    /**
     * 根据操作类型查找操作日志
     */
    List<OperationLog> findByOperationTypeOrderByOperationTimeDesc(String operationType);
    
    /**
     * 根据目标类型查找操作日志
     */
    List<OperationLog> findByTargetTypeOrderByOperationTimeDesc(String targetType);
    
    /**
     * 根据时间范围查找操作日志
     */
    List<OperationLog> findByOperationTimeBetweenOrderByOperationTimeDesc(
            LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据管理员ID和时间范围查找操作日志
     */
    List<OperationLog> findByAdminIdAndOperationTimeBetweenOrderByOperationTimeDesc(
            Long adminId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找所有操作日志（按时间倒序）
     */
    List<OperationLog> findAllByOrderByOperationTimeDesc();
    
    /**
     * 分页查找所有操作日志（按时间倒序）
     */
    Page<OperationLog> findAllByOrderByOperationTimeDesc(Pageable pageable);
    
    /**
     * 统计指定时间范围内的操作数量
     */
    @Query("SELECT COUNT(o) FROM OperationLog o WHERE o.operationTime BETWEEN :startTime AND :endTime")
    Long countByOperationTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定管理员在时间范围内的操作数量
     */
    @Query("SELECT COUNT(o) FROM OperationLog o WHERE o.adminId = :adminId AND o.operationTime BETWEEN :startTime AND :endTime")
    Long countByAdminIdAndOperationTimeBetween(@Param("adminId") Long adminId, 
                                              @Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);
}