package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.FileResource;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.service.FileResourceService;
import com.huadetec.gamecommunity.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import com.huadetec.gamecommunity.util.Result;

/**
 * 资源控制器
 * 处理资源上传和管理的API请求
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);
    
    // 上传文件存储目录 - 修改为前端assets/resources文件夹（使用绝对路径）
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/frontend/src/assets/resources/";
    
    @Autowired
    private FileResourceService fileResourceService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 处理资源上传请求
     */
    @PostMapping("/upload")
    public ResponseEntity<Result<Map<String, Object>>> uploadResource(@RequestParam("file") MultipartFile file) {
        logger.info("接收到资源上传请求，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Result.error(400, "文件不能为空"));
            }
            
            // 检查文件大小（限制为2GB）
            if (file.getSize() > 2L * 1024 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Result.error(400, "文件大小不能超过2GB"));
            }
            
            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uploader = "anonymous";
            Long uploaderId = null;
            if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                uploader = authentication.getName();
                // 获取用户ID
                Optional<User> userOptional = userService.findByUsername(uploader);
                if (userOptional.isPresent()) {
                    uploaderId = userOptional.get().getId();
                }
            }
            
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // 按日期创建子目录
            String dateDir = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            Path datePath = Paths.get(UPLOAD_DIR, dateDir);
            Files.createDirectories(datePath);
            
            // 保存文件
            Path filePath = datePath.resolve(uniqueFilename);
            file.transferTo(filePath.toFile());
            
            // 构建文件访问URL - 修改为前端assets/resources路径
            String fileUrl = "/assets/resources/" + dateDir + "/" + uniqueFilename;
            
            // 获取文件扩展名（去掉点号）
            String fileType = getFileExtension(originalFilename);
            
            // 保存文件信息到数据库
            FileResource fileResource = fileResourceService.createFileResource(
                originalFilename, 
                uniqueFilename, 
                filePath.toString(), 
                fileUrl, 
                file.getSize(), 
                fileType, 
                uploaderId, 
                uploader
            );
            
            // 构建响应数据
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("id", fileResource.getId());
            fileInfo.put("originalName", originalFilename);
            fileInfo.put("fileName", uniqueFilename);
            fileInfo.put("fileSize", file.getSize());
            fileInfo.put("fileUrl", fileUrl);
            fileInfo.put("uploadTime", fileResource.getUploadTime());
            fileInfo.put("uploader", uploader);
            fileInfo.put("uploaderId", uploaderId);
            fileInfo.put("fileType", fileType);
            
            logger.info("文件上传成功: {}，上传者: {}，文件ID: {}", fileUrl, uploader, fileResource.getId());
            
            return ResponseEntity.ok(Result.success(fileInfo));
            
        } catch (IOException e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(Result.error(500, "文件上传失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取资源列表（从数据库获取文件信息）
     */
    @GetMapping("/list")
    public ResponseEntity<Result<Map<String, Object>>> getResourceList() {
        logger.info("接收到资源列表请求，从数据库获取文件信息");
        
        try {
            // 从数据库获取所有未删除的文件资源
            List<FileResource> fileResources = fileResourceService.findAll();
            
            // 转换为前端需要的格式
            List<Map<String, Object>> resources = new ArrayList<>();
            for (FileResource fileResource : fileResources) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("id", fileResource.getId());
                fileInfo.put("originalName", fileResource.getOriginalName());
                fileInfo.put("fileName", fileResource.getFileName());
                fileInfo.put("fileSize", fileResource.getFileSize());
                fileInfo.put("fileUrl", fileResource.getFileUrl());
                fileInfo.put("uploadTime", fileResource.getUploadTime());
                fileInfo.put("uploader", fileResource.getUploaderName());
                fileInfo.put("uploaderId", fileResource.getUploaderId());
                fileInfo.put("fileType", fileResource.getFileType());
                fileInfo.put("category", fileResource.getCategory());
                fileInfo.put("description", fileResource.getDescription());
                fileInfo.put("downloadCount", fileResource.getDownloadCount());
                fileInfo.put("viewCount", fileResource.getViewCount());
                
                resources.add(fileInfo);
            }
            
            // 按上传时间排序（最新的在前）
            resources.sort((a, b) -> {
                Date dateA = (Date) a.get("uploadTime");
                Date dateB = (Date) b.get("uploadTime");
                return dateB.compareTo(dateA);
            });
            
            Map<String, Object> data = new HashMap<>();
            data.put("resources", resources);
            data.put("total", resources.size());
            
            logger.info("成功获取 {} 个资源文件", resources.size());
            
            return ResponseEntity.ok(Result.success(data));
            
        } catch (Exception e) {
            logger.error("获取资源列表失败: {}", e.getMessage(), e);
            
            Map<String, Object> data = new HashMap<>();
            data.put("resources", new Object[0]);
            data.put("total", 0);
            
            return ResponseEntity.internalServerError().body(Result.error(500, "获取资源列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 文件下载接口
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Object> downloadFile(@PathVariable Long fileId) {
        logger.info("接收到文件下载请求，文件ID: {}", fileId);
        
        try {
            // 根据文件ID获取文件信息
            Optional<FileResource> fileResourceOpt = fileResourceService.findById(fileId);
            if (!fileResourceOpt.isPresent()) {
                logger.warn("文件不存在，文件ID: {}", fileId);
                return ResponseEntity.notFound().build();
            }
            FileResource fileResource = fileResourceOpt.get();
            
            // 构建文件路径
            String filePath = fileResource.getFilePath();
            File file = new File(filePath);
            
            // 检查文件是否存在
            if (!file.exists()) {
                logger.warn("物理文件不存在，文件路径: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 增加下载计数
            fileResourceService.incrementDownloadCount(fileId);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + 
                URLEncoder.encode(fileResource.getOriginalName(), "UTF-8") + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");
            
            logger.info("文件下载成功，文件ID: {}, 原始文件名: {}", fileId, fileResource.getOriginalName());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .body(new FileSystemResource(file));
                    
        } catch (Exception e) {
            logger.error("文件下载失败，文件ID: {}, 错误: {}", fileId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("文件下载失败: " + e.getMessage());
        }
    }
    
    /**
     * 递归扫描目录获取文件信息
     */
    private void scanDirectory(File directory, List<Map<String, Object>> resources, String relativePath) {
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                // 递归扫描子目录
                String newRelativePath = relativePath.isEmpty() ? file.getName() : relativePath + "/" + file.getName();
                scanDirectory(file, resources, newRelativePath);
            } else if (file.isFile()) {
                // 添加文件信息
                Map<String, Object> fileInfo = new HashMap<>();
                
                // 生成唯一ID
                fileInfo.put("id", UUID.randomUUID().toString());
                fileInfo.put("name", file.getName());
                fileInfo.put("originalName", file.getName());
                fileInfo.put("fileName", file.getName());
                fileInfo.put("fileSize", file.length());
                fileInfo.put("fileType", getFileExtension(file.getName()));
                
                // 构建文件URL
                String fileUrl = "/assets/resources/" + relativePath + "/" + file.getName();
                fileInfo.put("fileUrl", fileUrl);
                
                // 获取文件修改时间作为上传时间
                fileInfo.put("uploadTime", new Date(file.lastModified()));
                
                // 设置上传者信息 - 这里简化处理，实际项目中应该从数据库或元数据文件中读取
                // 修改为基于文件命名规则提取上传者信息，让所有人都能看到相同的上传者信息
                String uploader = "unknown";
                
                // 检查文件名是否包含用户标识（例如：test1_1762059198378.png 中的test1）
                String filename = file.getName();
                if (filename.contains("_") && filename.indexOf("_") > 0) {
                    String prefix = filename.substring(0, filename.indexOf("_"));
                    // 如果前缀看起来像用户名（不是UUID格式）
                    if (!prefix.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                        uploader = prefix;
                    }
                }
                
                fileInfo.put("uploader", uploader);
                
                resources.add(fileInfo);
            }
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * 删除资源文件
     */
    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<Result<?>> deleteResource(@PathVariable String filename) {
        logger.info("接收到资源删除请求，文件名: {}", filename);
        
        try {
            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = "anonymous";
            boolean isAdmin = false;
            
            if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                currentUser = authentication.getName();
                // 检查是否为管理员（这里假设管理员用户名包含"admin"）
                isAdmin = currentUser.toLowerCase().contains("admin");
            }
            
            // 构建文件路径
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
            
            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(404).body(Result.error(404, "文件不存在"));
            }
            
            // 由于当前系统没有存储文件上传者信息，这里简化权限检查
            // 在实际系统中，应该从数据库或元数据文件中读取上传者信息
            // 这里假设所有文件都可以被管理员删除，普通用户只能删除自己上传的文件
            // 但由于没有存储上传者信息，暂时允许所有登录用户删除文件
            
            // 删除文件
            Files.delete(filePath);
            
            logger.info("文件删除成功: {}，操作者: {}", filename, currentUser);
            
            return ResponseEntity.ok(Result.success(null));
            
        } catch (IOException e) {
            logger.error("删除文件失败: {}", filename, e);
            return ResponseEntity.status(500).body(Result.error(500, "文件删除失败: " + e.getMessage()));
        }
    }
    

    
    /**
     * 递归查找文件
     */
    private File findFile(File directory, String filename) {
        File[] files = directory.listFiles();
        if (files == null) return null;
        
        for (File file : files) {
            if (file.isDirectory()) {
                File found = findFile(file, filename);
                if (found != null) return found;
            } else if (file.isFile() && file.getName().equals(filename)) {
                return file;
            }
        }
        return null;
    }
    
    /**
     * 获取原始文件名（从UUID文件名映射回原始文件名）
     */
    private String getOriginalFilename(String filename) {
        // 这里需要从数据库或文件映射中获取原始文件名
        // 暂时返回文件名本身
        return filename;
    }
}