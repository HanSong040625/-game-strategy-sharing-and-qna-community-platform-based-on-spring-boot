package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.OperationLog;
import com.huadetec.gamecommunity.repository.OperationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志服务类
 */
@Service
public class OperationLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(OperationLogService.class);
    
    @Autowired
    private OperationLogRepository operationLogRepository;
    
    /**
     * 记录操作日志
     */
    public OperationLog logOperation(Long adminId, String adminName, String operationType, 
                                   String targetType, Long targetId, String targetName, 
                                   String operationDetails, HttpServletRequest request) {
        try {
            OperationLog log = new OperationLog(adminId, adminName, operationType, targetType);
            log.setTargetId(targetId);
            log.setTargetName(targetName);
            log.setOperationDetails(operationDetails);
            
            // 设置请求信息
            if (request != null) {
                String ipAddress = getClientIpAddress(request);
                log.setIpAddress(ipAddress);
                log.setUserAgent(request.getHeader("User-Agent"));
            }
            
            OperationLog savedLog = operationLogRepository.save(log);
            return savedLog;
            
        } catch (Exception e) {
            throw e; // 重新抛出异常以便上层处理
        }
    }
    
    /**
     * 记录成功操作
     */
    public OperationLog logSuccessOperation(Long adminId, String adminName, String operationType, 
                                          String targetType, Long targetId, String targetName, 
                                          String operationDetails, HttpServletRequest request) {
        try {
            OperationLog log = logOperation(adminId, adminName, operationType, targetType, targetId, 
                                      targetName, operationDetails, request);
            log.setIsSuccess(true);
            
            OperationLog savedLog = operationLogRepository.save(log);
            return savedLog;
            
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * 记录失败操作
     */
    public OperationLog logFailedOperation(Long adminId, String adminName, String operationType, 
                                          String targetType, Long targetId, String targetName, 
                                          String operationDetails, String errorMessage, 
                                          HttpServletRequest request) {
        try {
            OperationLog log = logOperation(adminId, adminName, operationType, targetType, targetId, 
                                      targetName, operationDetails, request);
            log.setIsSuccess(false);
            log.setErrorMessage(errorMessage);
            
            OperationLog savedLog = operationLogRepository.save(log);
            return savedLog;
            
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * 获取所有操作日志
     */
    public List<OperationLog> getAllLogs() {
        return operationLogRepository.findAllByOrderByOperationTimeDesc();
    }
    
    /**
     * 分页获取所有操作日志
     */
    public Page<OperationLog> getLogsByPage(Pageable pageable) {
        return operationLogRepository.findAllByOrderByOperationTimeDesc(pageable);
    }
    
    /**
     * 根据管理员ID获取操作日志
     */
    public List<OperationLog> getLogsByAdminId(Long adminId) {
        return operationLogRepository.findByAdminIdOrderByOperationTimeDesc(adminId);
    }
    
    /**
     * 根据管理员ID分页获取操作日志
     */
    public Page<OperationLog> getLogsByAdminId(Long adminId, Pageable pageable) {
        return operationLogRepository.findByAdminIdOrderByOperationTimeDesc(adminId, pageable);
    }
    
    /**
     * 根据操作类型获取操作日志
     */
    public List<OperationLog> getLogsByOperationType(String operationType) {
        return operationLogRepository.findByOperationTypeOrderByOperationTimeDesc(operationType);
    }
    
    /**
     * 根据目标类型获取操作日志
     */
    public List<OperationLog> getLogsByTargetType(String targetType) {
        return operationLogRepository.findByTargetTypeOrderByOperationTimeDesc(targetType);
    }
    
    /**
     * 根据时间范围获取操作日志
     */
    public List<OperationLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogRepository.findByOperationTimeBetweenOrderByOperationTimeDesc(startTime, endTime);
    }
    
    /**
     * 根据管理员ID和时间范围获取操作日志
     */
    public List<OperationLog> getLogsByAdminIdAndTimeRange(Long adminId, LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogRepository.findByAdminIdAndOperationTimeBetweenOrderByOperationTimeDesc(adminId, startTime, endTime);
    }
    
    /**
     * 统计指定时间范围内的操作数量
     */
    public Long countOperationsInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogRepository.countByOperationTimeBetween(startTime, endTime);
    }
    
    /**
     * 统计指定管理员在时间范围内的操作数量
     */
    public Long countAdminOperationsInTimeRange(Long adminId, LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogRepository.countByAdminIdAndOperationTimeBetween(adminId, startTime, endTime);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 获取最近的操作日志
     * @param limit 获取的记录数量限制
     * @return 最近的操作日志列表
     */
    public List<OperationLog> findRecentLogs(int limit) {
        try {
            // 使用Spring Data JPA的分页功能来获取最近的日志
            org.springframework.data.domain.PageRequest pageRequest = 
                org.springframework.data.domain.PageRequest.of(0, limit);
            Page<OperationLog> page = operationLogRepository.findAllByOrderByOperationTimeDesc(pageRequest);
            logger.info("获取最近{}条操作日志，实际获取到{}条", limit, page.getContent().size());
            return page.getContent();
        } catch (Exception e) {
            logger.error("获取最近操作日志失败：异常信息={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}