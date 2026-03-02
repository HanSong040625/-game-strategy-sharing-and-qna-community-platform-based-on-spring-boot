package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.OperationLog;
import com.huadetec.gamecommunity.service.OperationLogService;
import com.huadetec.gamecommunity.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/admin/operation-logs")
public class OperationLogController {
    
    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 获取所有操作日志
     */
    @GetMapping
    public ResponseEntity<Result> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<OperationLog> logs = operationLogService.getLogsByPage(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("logs", logs.getContent());
        response.put("currentPage", logs.getNumber());
        response.put("totalItems", logs.getTotalElements());
        response.put("totalPages", logs.getTotalPages());
        
        return new ResponseEntity<>(Result.success(response), HttpStatus.OK);
    }
    
    /**
     * 根据管理员ID获取操作日志
     */
    @GetMapping("/admin/{adminId}")
    public ResponseEntity<Result> getLogsByAdminId(
            @PathVariable Long adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<OperationLog> logs = operationLogService.getLogsByAdminId(adminId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("logs", logs.getContent());
        response.put("currentPage", logs.getNumber());
        response.put("totalItems", logs.getTotalElements());
        response.put("totalPages", logs.getTotalPages());
        
        return new ResponseEntity<Result>(Result.success(response), HttpStatus.OK);
    }
    
    /**
     * 根据操作类型获取操作日志
     */
    @GetMapping("/type/{operationType}")
    public ResponseEntity<Result> getLogsByOperationType(@PathVariable String operationType) {
        List<OperationLog> logs = operationLogService.getLogsByOperationType(operationType);
        return new ResponseEntity<Result>(Result.success(logs), HttpStatus.OK);
    }
    
    /**
     * 根据目标类型获取操作日志
     */
    @GetMapping("/target/{targetType}")
    public ResponseEntity<Result> getLogsByTargetType(@PathVariable String targetType) {
        List<OperationLog> logs = operationLogService.getLogsByTargetType(targetType);
        return new ResponseEntity<Result>(Result.success(logs), HttpStatus.OK);
    }
    
    /**
     * 根据时间范围获取操作日志
     */
    @GetMapping("/time-range")
    public ResponseEntity<Result> getLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<OperationLog> logs = operationLogService.getLogsByTimeRange(startTime, endTime);
        return new ResponseEntity<Result>(Result.success(logs), HttpStatus.OK);
    }
    
    /**
     * 根据管理员ID和时间范围获取操作日志
     */
    @GetMapping("/admin/{adminId}/time-range")
    public ResponseEntity<Result> getLogsByAdminIdAndTimeRange(
            @PathVariable Long adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<OperationLog> logs = operationLogService.getLogsByAdminIdAndTimeRange(adminId, startTime, endTime);
        return new ResponseEntity<Result>(Result.success(logs), HttpStatus.OK);
    }
    
    /**
     * 统计操作日志
     */
    @GetMapping("/statistics")
    public ResponseEntity<Result> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Map<String, Object> statistics = new HashMap<>();
        
        if (startTime != null && endTime != null) {
            Long totalOperations = operationLogService.countOperationsInTimeRange(startTime, endTime);
            statistics.put("totalOperations", totalOperations);
            statistics.put("startTime", startTime);
            statistics.put("endTime", endTime);
        } else {
            // 默认统计最近30天的数据
            LocalDateTime defaultEndTime = LocalDateTime.now();
            LocalDateTime defaultStartTime = defaultEndTime.minusDays(30);
            Long totalOperations = operationLogService.countOperationsInTimeRange(defaultStartTime, defaultEndTime);
            statistics.put("totalOperations", totalOperations);
            statistics.put("startTime", defaultStartTime);
            statistics.put("endTime", defaultEndTime);
        }
        
        return new ResponseEntity<Result>(Result.success(statistics), HttpStatus.OK);
    }
    
    /**
     * 获取操作日志详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result> getLogDetail(@PathVariable Long id) {
        // 这里需要添加根据ID查找日志的逻辑
        // 由于日志不可删除，所以可以直接通过ID查找
        // 暂时返回空，实际实现需要添加相应的Repository方法
        return new ResponseEntity<Result>(Result.success(null), HttpStatus.OK);
    }
}