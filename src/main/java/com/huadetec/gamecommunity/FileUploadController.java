package com.huadetec.gamecommunity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping("/api")
public class FileUploadController {
    // 设置允许上传的文件类型
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    
    // 设置文件大小限制为10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PreAuthorize("permitAll()")
    @PostMapping("/upload")
    public ResponseEntity<String> handleUpload(@RequestParam("file") MultipartFile file) {
        // 参数校验
        if (file.isEmpty()) {
            return ResponseEntity.status(400).body("上传文件不能为空");
        }
        
        // 文件大小检查
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.status(400).body("文件大小不能超过10MB");
        }
        
        // 文件类型检查
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return ResponseEntity.status(400).body("不支持的文件类型");
        }

        String fileName = null;
        try {
            // 修改上传目录到前端项目的assets/covers文件夹
            String uploadDir = System.getProperty("user.dir") + "/frontend/src/assets/covers/";
            // 确保上传目录存在
            Files.createDirectories(Paths.get(uploadDir));
            
            // 获取原始文件名并生成新的文件名以避免冲突
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            // 使用UUID生成唯一文件名
            fileName = UUID.randomUUID().toString() + (extension != null ? "." + extension : "");
            
            Path path = Paths.get(uploadDir + fileName);
            file.transferTo(path);
            
            // 返回相对路径，前端可以直接访问
            return ResponseEntity.ok("/assets/covers/" + fileName);
        } catch (Exception e) {
            System.err.println("文件上传失败：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : null;
    }
}