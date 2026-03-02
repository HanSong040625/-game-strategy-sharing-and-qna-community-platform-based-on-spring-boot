package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.entity.Admin;
import com.huadetec.gamecommunity.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.huadetec.gamecommunity.service.AdminService;
import com.huadetec.gamecommunity.util.Result;

@RestController//这个类是专门处理前端 HTTP 请求的接口层，所有方法的返回值都会直接转换成 JSON/XML 数据，响应给前端（而不是渲染页面）
@RequestMapping("/api/auth")  // 所有接口路径前缀：/api/auth
// 移除@CrossOrigin注解，使用全局CORS配置
public class UserController {

    /**
     * 健康检查接口，用于前端检查后端服务状态（GET 请求）
     */
    @GetMapping("/health")
    public ResponseEntity<Result> healthCheck() {
        return new ResponseEntity<Result>(Result.success(null) , HttpStatus.OK);
    }

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AdminService adminService;

    /**
     * 用户登录接口（POST 请求）
     * 支持表单格式参数
     * 前端传参：username、password
     */
    @PostMapping("/login")
    public ResponseEntity<Result<Map<String, Object>>> login(@RequestParam String username, 
                                        @RequestParam String password,
                                        HttpServletRequest request) {
        // 参数验证
        if (username == null || password == null) {
            return new ResponseEntity<Result<Map<String , Object>>>(Result.error(400 , "用户名或密码不能为空") ,HttpStatus.BAD_REQUEST) ;
            
 
        }
        // 添加详细的请求日志，记录请求头信息
        try {
            logger.info("开始登录检查：用户名={}", username);
            User user = userService.findByUsername(username).orElse(null) ;
            logger.info("登录检查：用户={}, 用户对象={}", username, user);
            
            if (user != null) {
                logger.info("检测用户状态：username={}, isBanned={}", username, user.getIsBanned());
                if(user.getIsBanned() != null && user.getIsBanned() == 1){
                    logger.info("用户已被封禁，拒绝登录：{}", username);
                    return new ResponseEntity<Result<Map<String , Object>>>(Result.error(403 , "账号已被封禁") , HttpStatus.FORBIDDEN) ;
                } else {
                    logger.info("用户状态正常，允许登录：{}", username);
                }
            } else {
                logger.info("用户不存在：{}", username);
            }
            // 创建认证令牌
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            // 进行认证
            Authentication authentication = authenticationManager.authenticate(token);
            // 设置认证信息到上下文中
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 显式创建Session并保存认证信息，确保会话保持
            HttpSession session = request.getSession(true); // 强制创建Session
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext()); // 将会话上下文保存到Session
            session.setMaxInactiveInterval(1800); // 设置会话有效期为30分钟
            
            
            
            // 检查用户是否为管理员
            boolean isAdmin = false;
            try {
                // 首先检查是否是admin_user表中的用户
                if (adminService.findByUsername(username).isPresent()) {
                    isAdmin = true;
                } else {
                    // 检查普通用户表中的is_admin字段
                     user = userService.findByUsername(username).orElse(null);//有值返回用户对象，没有返回null

                    if (user != null && user.getIsAdmin() != null && user.getIsAdmin() == 1) {
                        isAdmin = true;
                    }
                }
            } catch (Exception e) {
                logger.warn("检查用户管理员状态失败：{}", e.getMessage());
            }
            
            // 返回包含isAdmin字段的完整响应
            Map<String, Object> data = new HashMap<>();
            data.put("username", username);
            data.put("isAdmin", isAdmin);
            return ResponseEntity.ok(Result.success(data));
        } catch (Exception e) {
            // 认证失败，返回错误信息和正确的HTTP状态码
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "用户名或密码错误"));
        }
    }



    
    /**
     * 用户注册接口（POST 请求）
     * 前端传参：username、password、email
     */
    @PostMapping("/register")
    public ResponseEntity<Result<?>> register(@RequestParam String username, 
                                          @RequestParam String password, 
                                          @RequestParam String email) {
        try {
            // 调用UserService进行用户注册
            User user = userService.register(username, password, email);
            return ResponseEntity.ok(Result.success(null));
        } catch (Exception e) {
            // 注册失败，返回错误信息
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        }
    }
    
    /**
     * 用户登出接口（POST 请求）
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<?>> logout(HttpServletRequest request) {
        try {
            
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                
                // 简化登出逻辑，不再更新离线状态
            // 直接使会话失效即可
            } else {
                logger.warn("认证信息无效或用户未登录");
            }
            
            // 使会话失效
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            // 清除安全上下文
            SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok(Result.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "登出失败"));
        }
    }
    
    /**
     * 获取当前登录用户信息接口（GET 请求）
     */
    @GetMapping("/current-user")
    public ResponseEntity<Result<Map<String, Object>>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            // 获取当前登录用户信息
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 尝试从数据库获取完整的用户信息，包括id
            try {
                User user = userService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("用户不存在"));
                
                // 返回包含id的完整用户信息
                Map<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("username", user.getUsername());
                return ResponseEntity.ok(Result.success(data));
            } catch (Exception e) {
                // 如果无法从数据库获取用户信息，返回基本用户名信息
                Map<String, Object> data = new HashMap<>();
                data.put("username", username);
                return ResponseEntity.ok(Result.success(data));
            }
        }
        // 未登录状态
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(401, "未登录"));
    }
    
    /**
     * 获取当前登录用户的完整信息（GET 请求）
     */
    @GetMapping("/current-user-full")
    public ResponseEntity<Result<Map<String, Object>>> getCurrentUserFull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            // 获取当前登录用户信息
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 尝试从普通用户表查询
            try {
                User user = userService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("用户不存在"));
                
                // 使用数据库中的is_admin字段判断用户是否为管理员
                // 0表示普通用户，1表示管理员
                boolean isAdminUser = user.getIsAdmin() != null && user.getIsAdmin() == 1;

                
                // 格式化日期为ISO 8601格式，确保前端可以正确解析
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = dateFormat.format(user.getCreateTime());
                
                // 构建响应数据，注意不包含密码
                // 使用实际的管理员状态，并添加isOnline字段和avatarUrl字段
                Map<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("username", user.getUsername());
                data.put("email", user.getEmail());
                data.put("createTime", formattedDate);
                data.put("isAdmin", isAdminUser);
                data.put("isOnline", user.getIsBanned() != null ? user.getIsBanned() : 0);
                data.put("avatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
                data.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
                
                return ResponseEntity.ok(Result.success(data));
            } catch (Exception e) {
                // 如果普通用户表中不存在，可能是admin_user表中的用户
            
                
                // 尝试从admin_user表获取用户信息
                try {
                    Admin adminUser = adminService.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("管理员用户不存在"));
                    
                    // 对于admin_user表中的用户，返回包含id的完整信息
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", adminUser.getId());
                    data.put("username", username);
                    data.put("isAdmin", true);
                    
                    return ResponseEntity.ok(Result.success(data));
                } catch (Exception adminException) {
                    // 如果admin_user表中也不存在，说明用户不存在于任何表中
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Result.error(404, "用户不存在"));
                }
            }
        }
        // 未登录状态
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(401, "未登录"));
    }
    
    /**
     * 修改密码接口（POST 请求）
     */
    @PostMapping("/change-password")
    public ResponseEntity<Result<?>> changePassword(@RequestBody Map<String, String> requestBody) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || (authentication.getPrincipal() instanceof String)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未登录"));
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 获取请求参数
            String currentPassword = requestBody.get("currentPassword");
            String newPassword = requestBody.get("newPassword");
            
            // 参数校验
            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "参数不能为空"));
            }
            
            // 验证当前密码
            boolean passwordMatch = userService.verifyPassword(username, currentPassword);
            if (!passwordMatch) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "当前密码不正确"));
            }
            
            // 更新密码
            userService.updatePassword(username, newPassword);
            
            return ResponseEntity.ok(Result.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "修改密码失败"));
        }
    }
    
    /**
     * 更新用户个人信息接口（POST 请求）
     * 前端传参：username、email
     */
    @PostMapping("/update-profile")
    public ResponseEntity<Result<?>> updateProfile(@RequestBody Map<String, String> requestBody, 
                                               Authentication authentication) {
        try {
            // 获取当前登录用户名
            String currentUsername = authentication.getName();
            
            // 获取请求参数
            String newUsername = requestBody.get("username");
            String newEmail = requestBody.get("email");
            
            // 参数校验 - 只检查用户名和邮箱是否为空
            if (newUsername == null || newUsername.trim().isEmpty() || newEmail == null || newEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "用户名和邮箱不能为空"));
            }
            
            // 检查用户名和邮箱是否已被使用（排除当前用户）
            if (!currentUsername.equals(newUsername) && userService.existsByUsername(newUsername)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "用户名已被占用"));
            }
            
            // 检查邮箱唯一性（排除当前用户）
            Optional<User> currentUserOpt = userService.findByUsername(currentUsername);
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                if (!currentUser.getEmail().equals(newEmail) && userService.existsByEmail(newEmail)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Result.error(400, "邮箱已被注册"));
                }
            }
            
            // 调用UserService更新个人信息
            userService.updateProfile(currentUsername, newUsername, newEmail);
            
            // 如果用户名变更，需要刷新认证上下文
            if (!currentUsername.equals(newUsername)) {
                // 重新加载用户信息
                User updatedUser = userService.findByUsername(newUsername)
                        .orElseThrow(() -> new RuntimeException("用户不存在"));
                
                // 创建新的认证令牌
                UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                        updatedUser.getUsername(), null, authentication.getAuthorities());
                newAuth.setDetails(authentication.getDetails());
                
                // 更新SecurityContext中的认证信息
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                
            }
            return ResponseEntity.ok(Result.success(null));  
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }
     
     /**
     * 更新用户在线状态接口
     */
    @GetMapping("/update-online-status")
    public ResponseEntity<Result<Map<String, Object>>> updateOnlineStatus(@RequestParam Integer status) {
        try {
            System.out.println("[UPDATE STATUS API] 接收到更新在线状态请求，状态值: " + status);
            
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                
                System.out.println("[UPDATE STATUS API] 当前登录用户: " + username);
                
                // 更新用户在线状态
            User user = userService.findByUsername(username).orElse(null);
            if (user != null) {
                System.out.println("[UPDATE STATUS API] 更新前状态: " + user.getIsBanned());
                user.setIsBanned(status);
                User savedUser = userService.save(user);
                System.out.println("[UPDATE STATUS API] 更新后状态: " + savedUser.getIsBanned());
                
                Map<String, Object> data = new HashMap<>();
                data.put("isOnline", 1);
                return ResponseEntity.ok(Result.success(data));
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Result.error(404, "用户不存在"));
                }
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录"));
        } catch (Exception e) {
            System.out.println("[UPDATE STATUS API] 更新在线状态时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }
    
    /**
     * 调试接口：获取用户在线状态
     */
    @GetMapping("/debug/online-status/{username}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result<Map<String, Object>>> getOnlineStatus(@PathVariable String username) {
        try {
            System.out.println("[DEBUG API] 查询用户在线状态: " + username);
            User user = userService.findByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("[DEBUG API] 用户不存在: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(404, "用户不存在"));
            }
            Integer isBanned = user.getIsBanned();
            Date lastLogin = user.getLastLogin();
            System.out.println("[DEBUG API] 用户" + username + "当前状态: " + isBanned + ", 最后登录时间: " + lastLogin);
            
            Map<String, Object> data = new HashMap<>();
            data.put("username", username);
            data.put("isBanned", isBanned != null ? isBanned : 0);
            data.put("lastLogin", lastLogin != null ? lastLogin.toString() : null);
            
            System.out.println("[DEBUG API] 返回在线状态响应: " + data);
              
             return ResponseEntity.ok(Result.success(data));
         } catch (Exception e) {
             System.out.println("查询用户在线状态时发生错误: " + e.getMessage());
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error(500, "服务器内部错误"));
         }
     }
    
    /**
     * 获取用户在数据库中的完整信息（用于调试）
     */
    @GetMapping("/get-user-info/{username}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result<User>> getUserInfo(@PathVariable String username) {
        try {
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("[DEBUG] 获取用户完整信息: " + username + ", isBanned: " + user.getIsBanned() + ", lastLogin: " + user.getLastLogin());
                return ResponseEntity.ok(Result.success(user));
            } else {
                System.out.println("[DEBUG] 用户不存在: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(404, "用户不存在"));
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] 获取用户完整信息异常: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error(500, "服务器内部错误"));
        }
    }
    

    
    /**
     * 头像上传接口（POST 请求）
     */
    @PostMapping("/upload-avatar")
    public ResponseEntity<Result<Map<String, Object>>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || (authentication.getPrincipal() instanceof String)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未登录"));
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "只支持JPG、JPEG、PNG、GIF格式的图片"));
            }
            
            // 验证文件大小（限制为2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "图片大小不能超过2MB"));
            }
            
            // 生成文件名（用户名+时间戳+扩展名）
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = username + "_" + System.currentTimeMillis() + fileExtension;
            
            // 头像保存路径（同时保存到frontend/src/assets/avatars和frontend/dist/assets/avatars文件夹）
            String srcAvatarsDir = System.getProperty("user.dir") + "/frontend/src/assets/avatars/";
            String distAvatarsDir = System.getProperty("user.dir") + "/frontend/dist/assets/avatars/";
            
            java.nio.file.Path srcAvatarsPath = java.nio.file.Paths.get(srcAvatarsDir);
            java.nio.file.Path distAvatarsPath = java.nio.file.Paths.get(distAvatarsDir);
            
            // 创建src目录（如果不存在）
            if (!java.nio.file.Files.exists(srcAvatarsPath)) {
                java.nio.file.Files.createDirectories(srcAvatarsPath);
            }
            
            // 创建dist目录（如果不存在）
            if (!java.nio.file.Files.exists(distAvatarsPath)) {
                java.nio.file.Files.createDirectories(distAvatarsPath);
            }
            
            // 保存文件到frontend/src/assets/avatars文件夹
            java.nio.file.Path srcFilePath = srcAvatarsPath.resolve(fileName);
            file.transferTo(srcFilePath.toFile());
            
            // 同时复制文件到frontend/dist/assets/avatars文件夹
            java.nio.file.Path distFilePath = distAvatarsPath.resolve(fileName);
            java.nio.file.Files.copy(srcFilePath, distFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // 生成头像访问URL（使用/assets/avatars路径，这样前端可以直接访问）
            String avatarUrl = "/assets/avatars/" + fileName;
            
            // 更新用户头像URL到数据库
            boolean updateResult = userService.updateAvatar(username, avatarUrl);
            
            if (updateResult) {
                Map<String, Object> data = new HashMap<>();
                data.put("avatarUrl", avatarUrl);
                return ResponseEntity.ok(Result.success(data));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Result.error(500, "头像更新失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "头像更新失败"));
        }
    }
    
    /**
     * 更新用户头像URL接口（POST 请求）
     * 用于更新已上传的头像URL到用户信息中
     */
    @PostMapping("/update-avatar")
    public ResponseEntity<Result<Map<String, Object>>> updateAvatar(@RequestBody Map<String, String> requestBody) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || (authentication.getPrincipal() instanceof String)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未登录"));
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 获取头像URL
            String avatarUrl = requestBody.get("avatarUrl");
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "头像URL不能为空"));
            }
            
            // 更新用户头像URL到数据库
            boolean updateResult = userService.updateAvatar(username, avatarUrl);
            
            if (updateResult) {
                Map<String, Object> data = new HashMap<>();
                data.put("avatarUrl", avatarUrl);
                return ResponseEntity.ok(Result.success(data));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Result.error(500, "头像更新失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "头像更新失败"));
        }
    }
    
    /**
     * 获取指定用户信息接口（GET 请求）
     * 通过用户ID获取用户信息
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result<Map<String, Object>>> getUserById(@PathVariable Long userId) {
        try {
            // 查找用户
            Optional<User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.error(404, "用户不存在"));
            }
            
            User user = userOptional.get();
            
            // 格式化日期
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(user.getCreateTime());
            
            // 构建响应数据，注意不包含密码
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            data.put("createTime", formattedDate);
            data.put("avatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
            
            return ResponseEntity.ok(Result.success(data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取用户信息失败"));
        }
    }
    
    /**
     * 根据用户名获取用户信息接口（GET 请求）
     * 通过用户名获取用户信息
     */
    @GetMapping("/user/by-username/{username}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result<Map<String, Object>>> getUserByUsername(@PathVariable String username) {
        try {
            // 查找用户
            Optional<User> userOptional = userService.findByUsername(username);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.error(404, "用户不存在"));
            }
            
            User user = userOptional.get();
            
            // 格式化日期
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(user.getCreateTime());
            
            // 构建响应数据，注意不包含密码
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            data.put("createTime", formattedDate);
            data.put("avatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
            
            return ResponseEntity.ok(Result.success(data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取用户信息失败"));
        }
    }
    
    /**
     * 获取用户列表接口（GET 请求）
     * 用于私聊功能选择用户
     */
    @GetMapping("/users/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<List<Map<String, Object>>>> getUserList() {
        try {
            
            // 获取所有用户
            List<User> users = userService.findAllUsers();
            
            // 构建响应数据列表
            List<Map<String, Object>> userList = new ArrayList<>();
            
            for (User user : users) {
                // 格式化日期
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedCreateTime = user.getCreateTime() != null ? dateFormat.format(user.getCreateTime()) : "";
                String formattedLastLogin = user.getLastLogin() != null ? dateFormat.format(user.getLastLogin()) : "";
                
                // 检查是否是管理员用户
                boolean isAdmin = false;
                try {
                    if (adminService.findByUsername(user.getUsername()).isPresent()) {
                        isAdmin = true;
                    } else if (user.getIsAdmin() != null && user.getIsAdmin() == 1) {
                        isAdmin = true;
                    }
                } catch (Exception e) {
                    logger.warn("检查用户管理员状态失败：{}", e.getMessage());
                }
                
                // 构建用户数据对象
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("email", user.getEmail());
                userData.put("registrationDate", formattedCreateTime);
                userData.put("lastLogin", formattedLastLogin);
                userData.put("isAdmin", isAdmin);
                userData.put("isOnline", user.getIsBanned() != null ? user.getIsBanned() : 0);
                
                userList.add(userData);
            }
            
            return ResponseEntity.ok(Result.success(userList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取用户列表失败"));
        }
    }
}