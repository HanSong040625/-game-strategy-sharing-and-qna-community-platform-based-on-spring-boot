package com.huadetec.gamecommunity.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 记录管理员的所有操作，不可删除
 */
@Entity
@Table(name = "operation_log")
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "admin_id", nullable = false)
    private Long adminId;
    
    @Column(name = "admin_name", nullable = false, length = 50)
    private String adminName;
    
    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;
    
    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;
    
    @Column(name = "target_id")
    private Long targetId;
    
    @Column(name = "target_name", length = 100)
    private String targetName;
    
    @Column(name = "operation_details", columnDefinition = "TEXT")
    private String operationDetails;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;
    
    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    // 构造函数
    public OperationLog() {
        this.operationTime = LocalDateTime.now();
        this.isSuccess = true;
    }
    
    public OperationLog(Long adminId, String adminName, String operationType, String targetType) {
        this();
        this.adminId = adminId;
        this.adminName = adminName;
        this.operationType = operationType;
        this.targetType = targetType;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
    
    public String getAdminName() {
        return adminName;
    }
    
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    
    public Long getTargetId() {
        return targetId;
    }
    
    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
    
    public String getTargetName() {
        return targetName;
    }
    
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    
    public String getOperationDetails() {
        return operationDetails;
    }
    
    public void setOperationDetails(String operationDetails) {
        this.operationDetails = operationDetails;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public LocalDateTime getOperationTime() {
        return operationTime;
    }
    
    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }
    
    public Boolean getIsSuccess() {
        return isSuccess;
    }
    
    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}