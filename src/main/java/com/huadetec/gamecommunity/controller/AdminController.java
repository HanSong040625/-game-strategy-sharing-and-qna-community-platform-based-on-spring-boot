package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.Admin;
import com.huadetec.gamecommunity.entity.Game;
import com.huadetec.gamecommunity.entity.OperationLog;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.repository.GameRepository;
import com.huadetec.gamecommunity.repository.GuideRepository;
import com.huadetec.gamecommunity.repository.QuestionRepository;
import com.huadetec.gamecommunity.service.AdminService;
import com.huadetec.gamecommunity.service.OperationLogService;
import com.huadetec.gamecommunity.service.UserService;
import com.huadetec.gamecommunity.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 管理员登录接口（POST 请求）
     * 前端传参：username、password
     */
    @PostMapping("/login")
    public ResponseEntity<Result<Map<String, Object>>> login(@RequestParam String username, @RequestParam String password, HttpServletRequest request) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return new ResponseEntity<>(Result.error(400, "用户名与密码不能为空"), HttpStatus.BAD_REQUEST);
        }

        try {
            // 首先检查管理员用户是否存在
            Optional<Admin> adminOptional = adminService.findByUsername(username);
            if (!adminOptional.isPresent()) {
                logger.warn("在admin_user表中未找到管理员用户：{}", username);
                return new ResponseEntity<>(Result.error(401, "用户名或密码错误"), HttpStatus.UNAUTHORIZED);
            }

            Admin admin = adminOptional.get();

            // 验证密码
            boolean passwordMatch = adminService.verifyPassword(username, password);
            logger.info("密码验证结果：{}", passwordMatch);

            if (!passwordMatch) {
                logger.warn("管理员密码验证失败：{}", username);
                return new ResponseEntity<>(Result.error(401, "用户名或密码错误"), HttpStatus.UNAUTHORIZED);
            }

            // 只有在管理员表中找到用户且密码正确时，才进行认证
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 创建Session并保存认证信息
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            session.setMaxInactiveInterval(1800);

            logger.info("管理员登录成功，会话ID：{}", session.getId());

            // 记录管理员登录操作日志
            try {
                // 获取管理员ID - 使用之前已经查询到的admin对象
                if (adminOptional.isPresent()) {
                    Admin adminForLog = adminOptional.get();
                    operationLogService.logSuccessOperation(
                        adminForLog.getId(), 
                        username, 
                        "ADMIN_LOGIN", 
                        "SYSTEM", 
                        null, 
                        "管理员登录", 
                        "管理员用户登录系统成功", 
                        request
                    );
                    logger.info("管理员登录操作日志记录成功，管理员ID：{}", adminForLog.getId());
                }
            } catch (Exception e) {
                logger.error("记录管理员登录操作日志失败：{}", e.getMessage());
                // 不抛出异常，避免影响登录流程
            }

            // 生成简单的token
            String token = System.currentTimeMillis() + "-" + username;

            // 创建返回数据对象
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", username);

            // 返回成功响应，包含token和username
            return new ResponseEntity<>(Result.success(data), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("管理员登录失败：{}", e.getMessage(), e);
            // 认证失败，返回简洁的错误信息
            return new ResponseEntity<>(Result.error(401, "用户名或密码错误"), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 管理员退出登录接口（POST 请求）
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 记录退出日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                Optional<Admin> adminOptional = adminService.findByUsername(username);
                if (adminOptional.isPresent()) {
                    Admin admin = adminOptional.get();
                    operationLogService.logSuccessOperation(
                        admin.getId(),
                        username,
                        "ADMIN_LOGOUT",
                        "SYSTEM",
                        null,
                        "管理员退出",
                        "管理员用户退出系统成功",
                        request
                    );
                }
            }

            // 清除认证信息
            SecurityContextHolder.clearContext();

            // 使Session失效
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // 清除前端存储的token
            response.setHeader("Set-Cookie", "token=; Max-Age=0; Path=/; HttpOnly");

            return new ResponseEntity<>(Result.success(null), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("管理员退出失败：{}", e.getMessage(), e);
            return new ResponseEntity<>(Result.error(500, "退出失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取当前登录管理员信息（GET 请求）
     */
    @GetMapping("/current-admin")
    public ResponseEntity<Result<String>> getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            // 获取当前登录管理员信息
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return new ResponseEntity<>(Result.success(userDetails.getUsername()), HttpStatus.OK);
        }
        // 未登录状态
        return new ResponseEntity<>(Result.error(401, "未登录"), HttpStatus.UNAUTHORIZED);
    }

    /**
     * 获取当前登录管理员的完整信息（GET 请求）
     */
    @GetMapping("/current-admin-full")
    public ResponseEntity<Result<Admin>> getCurrentAdminFull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            // 获取当前登录管理员信息
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 查询管理员详细信息
            Optional<Admin> adminOptional = adminService.findByUsername(username);
            if (adminOptional.isPresent()) {
                Admin admin = adminOptional.get();
                return new ResponseEntity<>(Result.success(admin), HttpStatus.OK);
            }
        }
        // 未登录状态或管理员不存在
        return new ResponseEntity<>(Result.error(401, "未登录"), HttpStatus.UNAUTHORIZED);
    }

    /**
     * 管理员管理接口 - 创建新管理员（POST 请求）
     * 前端传参：username、password、email
     */
    @PostMapping("/admins")
    public ResponseEntity<Result<Admin>> createAdmin(@RequestParam String username, @RequestParam String password, @RequestParam String email, HttpServletRequest request) {
        try {
            // 权限检查：只有超级管理员可以创建新管理员
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
                return new ResponseEntity<>(Result.error(401, "未登录"), HttpStatus.UNAUTHORIZED);
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String currentUsername = userDetails.getUsername();
            Optional<Admin> currentAdminOptional = adminService.findByUsername(currentUsername);
            if (!currentAdminOptional.isPresent() || currentAdminOptional.get().getRole() != 1) {
                return new ResponseEntity<>(Result.error(403, "权限不足"), HttpStatus.FORBIDDEN);
            }

            // 验证参数
            if (username == null || username.trim().isEmpty()) {
                return new ResponseEntity<>(Result.error(400, "用户名不能为空"), HttpStatus.BAD_REQUEST);
            }
            if (password == null || password.trim().isEmpty()) {
                return new ResponseEntity<>(Result.error(400, "密码不能为空"), HttpStatus.BAD_REQUEST);
            }
            if (email == null || email.trim().isEmpty()) {
                return new ResponseEntity<>(Result.error(400, "邮箱不能为空"), HttpStatus.BAD_REQUEST);
            }

            // 检查用户名是否已存在
            Optional<Admin> existingAdmin = adminService.findByUsername(username);
            if (existingAdmin.isPresent()) {
                return new ResponseEntity<>(Result.error(400, "用户名已存在"), HttpStatus.BAD_REQUEST);
            }

            // 创建新管理员
            Admin newAdmin = new Admin();
            newAdmin.setUsername(username);
            newAdmin.setPassword(passwordEncoder.encode(password));
            newAdmin.setEmail(email);
            newAdmin.setRole(2); // 默认普通管理员
            newAdmin.setStatus(1);
            newAdmin.setCreateTime(new java.util.Date());

            Admin savedAdmin = adminService.save(newAdmin);

            // 记录操作日志
            Admin currentAdmin = currentAdminOptional.get();
            operationLogService.logSuccessOperation(
                currentAdmin.getId(),
                currentUsername,
                "CREATE_ADMIN",
                "SYSTEM",
                savedAdmin.getId(),
                "创建管理员",
                "创建新管理员用户：" + username,
                request
            );

            return new ResponseEntity<>(Result.success(savedAdmin), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("创建管理员失败：{}", e.getMessage(), e);
            return new ResponseEntity<>(Result.error(500, "创建失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 管理员管理接口 - 获取管理员列表（GET 请求）
     */
    @GetMapping("/admins")
    public ResponseEntity<Result<List<Admin>>> getAdmins() {
        try {
            // 权限检查：只有超级管理员可以查看管理员列表
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
                return new ResponseEntity<>(Result.error(401, "未登录"), HttpStatus.UNAUTHORIZED);
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String currentUsername = userDetails.getUsername();
            Optional<Admin> currentAdminOptional = adminService.findByUsername(currentUsername);
            if (!currentAdminOptional.isPresent() || currentAdminOptional.get().getRole() != 1) {
                return new ResponseEntity<>(Result.error(403, "权限不足"), HttpStatus.FORBIDDEN);
            }

            List<Admin> admins = adminService.findAllAdmins();
            return new ResponseEntity<>(Result.success(admins), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取管理员列表失败：{}", e.getMessage(), e);
            return new ResponseEntity<>(Result.error(500, "获取失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 管理员管理接口 - 删除管理员（DELETE 请求）
     */
    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Result<?>> deleteAdmin(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 权限检查：只有超级管理员可以删除管理员
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
                return new ResponseEntity<>(Result.error(401, "未登录"), HttpStatus.UNAUTHORIZED);
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String currentUsername = userDetails.getUsername();
            Optional<Admin> currentAdminOptional = adminService.findByUsername(currentUsername);
            if (!currentAdminOptional.isPresent() || currentAdminOptional.get().getRole() != 1) {
                return new ResponseEntity<>(Result.error(403, "权限不足"), HttpStatus.FORBIDDEN);
            }

            // 不能删除超级管理员
            Optional<Admin> adminToDelete = adminService.findById(id);
            if (!adminToDelete.isPresent()) {
                return new ResponseEntity<>(Result.error(404, "管理员不存在"), HttpStatus.NOT_FOUND);
            }
            if (adminToDelete.get().getRole() == 1) {
                return new ResponseEntity<>(Result.error(400, "不能删除超级管理员"), HttpStatus.BAD_REQUEST);
            }

            // 删除管理员
            adminService.delete(id);

            // 记录操作日志
            Admin currentAdmin = currentAdminOptional.get();
            operationLogService.logSuccessOperation(
                currentAdmin.getId(),
                currentUsername,
                "DELETE_ADMIN",
                "SYSTEM",
                id,
                "删除管理员",
                "删除管理员ID：" + id,
                request
            );

            return new ResponseEntity<>(Result.success(null), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("删除管理员失败：{}", e.getMessage(), e);
            return new ResponseEntity<>(Result.error(500, "删除失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 用户管理接口 - 获取用户列表（GET 请求）
     */
    @GetMapping("/users")
    public ResponseEntity<Result<Map<String, Object>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 权限检查
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
                return new ResponseEntity<>(Result.error(401, "未登录"), HttpStatus.UNAUTHORIZED);
            }

            // 分页查询用户列表
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            Page<User> userPage = userService.findAllUsers(page, pageSize);
            List<User> users = userPage.getContent();

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("users", users);
            data.put("total", userPage.getTotalElements());

            return new ResponseEntity<>(Result.success(data), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取用户列表失败：{}", e.getMessage(), e);
            return new ResponseEntity<>(Result.error(500, "获取失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 用户管理接口 - 禁用/启用用户（PUT 请求）
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<Result<User>> updateUserStatus(@PathVariable Long id, @RequestParam int status, HttpServletRequest request) {
        try {
            // 权限检查
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
                return new ResponseEntity<>(Result.error(401, "未登录"), HttpStatus.UNAUTHORIZED);
            }

            // 验证状态值
            if (status != 0 && status != 1) {
                return new ResponseEntity<>(Result.error(400, "状态值无效"), HttpStatus.BAD_REQUEST);
            }

            // 更新用户状态
            User user = userService.findById(id).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(Result.error(404, "用户不存在"), HttpStatus.NOT_FOUND);
            }

            user.setIsBanned(status);
            userService.save(user);

            // 记录操作日志
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String currentUsername = userDetails.getUsername();
            Optional<Admin> currentAdminOptional = adminService.findByUsername(currentUsername);
            if (currentAdminOptional.isPresent()) {
                Admin currentAdmin = currentAdminOptional.get();
                operationLogService.logSuccessOperation(
                    currentAdmin.getId(),
                    currentUsername,
                    "UPDATE_USER_STATUS",
                    "SYSTEM",
                    id,
                    "更新用户状态",
                    "将用户ID：" + id + "状态更新为：" + (status == 1 ? "启用" : "禁用"),
                    request
                );
            }

            return new ResponseEntity<>(Result.success(user), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("更新用户状态失败：{}", e.getMessage(), e);
            return new ResponseEntity<>(Result.error(500, "更新失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 数据统计接口 - 获取系统概览数据（GET 请求）
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Result<Map<String, Object>>> getDashboardStats() {
        try {
            // 权限检查
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
                return new ResponseEntity<>(Result.error(401, "未登录"), HttpStatus.UNAUTHORIZED);
            }

            // 统计数据
            long userCount = userService.countAllUsers();
            long gameCount = gameRepository.count();
            long guideCount = guideRepository.count();
            long questionCount = questionRepository.count();
            long adminCount = adminService.count();

            // 构建返回数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("userCount", userCount);
            stats.put("gameCount", gameCount);
            stats.put("guideCount", guideCount);
            stats.put("questionCount", questionCount);
            stats.put("adminCount", adminCount);

            return new ResponseEntity<>(Result.success(stats), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取系统概览数据失败：{}", e.getMessage(), e);
            return new ResponseEntity<>(Result.error(500, "获取失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 操作日志接口 - 获取操作日志列表（GET 请求）
     */
    @GetMapping("/logs")
    public ResponseEntity<Result<Map<String, Object>>> getOperationLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 权限检查
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
                return new ResponseEntity<>(Result.error(401, "未登录"), HttpStatus.UNAUTHORIZED);
            }

            // 分页查询操作日志
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            Page<OperationLog> logPage = operationLogService.getLogsByPage(pageable);
            List<OperationLog> logs = logPage.getContent();

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("logs", logs);
            data.put("total", logPage.getTotalElements());

            return new ResponseEntity<>(Result.success(data), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取操作日志失败：{}", e.getMessage(), e);
            return new ResponseEntity<>(Result.error(500, "获取失败"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
