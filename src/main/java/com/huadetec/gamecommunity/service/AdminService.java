package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.Admin;
import com.huadetec.gamecommunity.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service  // 标记为服务层组件
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 管理员注册
     * @param username 用户名
     * @param password 原始密码
     * @param email 邮箱
     * @return 注册成功的管理员用户
     */
    public Admin register(String username, String password, String email) {
        // 1. 校验参数：用户名、密码、邮箱不能为空
        Assert.hasText(username, "用户名不能为空");
        Assert.hasText(password, "密码不能为空");
        Assert.hasText(email, "邮箱不能为空");

        // 2. 校验用户名、邮箱是否已存在
        if (adminRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已被占用");
        }
        if (adminRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 3. 对密码进行加密后保存到数据库
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));  // 加密存储密码
        admin.setEmail(email);
        return adminRepository.save(admin);
    }

    /**
     * 根据用户名查询管理员（登录验证用）
     */
    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }
    
    /**
     * 验证管理员密码是否正确
     * @param username 用户名
     * @param password 待验证的密码
     * @return 密码是否正确
     */
    public boolean verifyPassword(String username, String password) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        // 使用BCrypt验证密码
        return passwordEncoder.matches(password, admin.getPassword());
    }

    /**
     * 更新管理员密码
     * @param username 用户名
     * @param newPassword 新密码
     */
    public void updatePassword(String username, String newPassword) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 对新密码进行加密后存储
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }

    /**
     * 更新管理员个人信息
     * @param currentUsername 当前用户名
     * @param newUsername 新用户名
     * @param newEmail 新邮箱
     */
    public void updateProfile(String currentUsername, String newUsername, String newEmail) {
        // 参数验证，确保新用户名和新邮箱不为空
        Assert.hasText(newUsername, "新用户名不能为空");
        Assert.hasText(newEmail, "新邮箱不能为空");
        
        // 验证新用户名和当前用户名是否相同，如果不同则检查是否已存在
        if (!newUsername.equals(currentUsername) && adminRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("新用户名已被占用");
        }
        
        // 查找当前管理员
        Admin admin = adminRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 更新用户名和邮箱
        admin.setUsername(newUsername);
        admin.setEmail(newEmail);
        adminRepository.save(admin);
    }

    /**
     * 检查用户名是否已存在
     * @param username 要检查的用户名
     * @return 是否存在
     */
    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否已存在
     * @param email 要检查的邮箱
     * @return 是否存在
     */
    public boolean existsByEmail(String email) {
        return adminRepository.existsByEmail(email);
    }
    
    /**
     * 检查指定用户名是否为管理员用户
     * @param username 要检查的用户名
     * @return 是否为管理员用户
     */
    public boolean isAdminUser(String username) {
        return adminRepository.existsByUsername(username);
    }
    
    /**
     * 获取所有管理员用户
     * @return 管理员用户列表
     */
    public List<Admin> findAllAdmins() {
        return adminRepository.findAll();
    }
    
    /**
     * 根据ID获取管理员用户
     * @param id 管理员ID
     * @return 管理员用户
     */
    public Optional<Admin> findById(Long id) {
        return adminRepository.findById(id);
    }
    
    /**
     * 获取管理员用户总数
     * @return 管理员用户总数
     */
    public long count() {
        return adminRepository.count();
    }
    
    /**
     * 保存管理员用户
     * @param admin 管理员用户
     * @return 保存后的管理员用户
     */
    public Admin save(Admin admin) {
        return adminRepository.save(admin);
    }
    
    /**
     * 删除管理员用户
     * @param id 管理员ID
     */
    public void delete(Long id) {
        adminRepository.deleteById(id);
    }
}