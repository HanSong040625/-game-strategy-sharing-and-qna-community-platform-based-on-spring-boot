package com.huadetec.gamecommunity.util;

import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 密码迁移工具
 * 将现有用户的明文密码转换为BCrypt加密格式
 */
@Component
public class PasswordMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PasswordMigration.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("开始检查用户密码加密状态...");
        
        // 获取所有用户
        List<User> users = userRepository.findAll();
        logger.info("找到 {} 个用户需要检查", users.size());
        
        int migratedCount = 0;
        
        for (User user : users) {
            String currentPassword = user.getPassword();
            
            // 检查密码是否已经是BCrypt格式
            if (isBcryptEncoded(currentPassword)) {
                logger.info("用户 {} 的密码已经是BCrypt格式，跳过", user.getUsername());
                continue;
            }
            
            // 如果是明文密码，进行加密
            logger.info("用户 {} 的密码是明文，开始加密...", user.getUsername());
            String encryptedPassword = passwordEncoder.encode(currentPassword);
            user.setPassword(encryptedPassword);
            userRepository.save(user);
            
            logger.info("用户 {} 密码加密完成: {} -> {}", 
                user.getUsername(), currentPassword, encryptedPassword);
            migratedCount++;
        }
        
        logger.info("密码迁移完成！共迁移了 {} 个用户的密码", migratedCount);
    }
    
    /**
     * 检查字符串是否为BCrypt加密格式
     */
    private boolean isBcryptEncoded(String password) {
        if (password == null || password.length() < 4) {
            return false;
        }
        
        // BCrypt格式通常以 $2a$, $2b$, $2y$ 开头
        return password.startsWith("$2a$") || 
               password.startsWith("$2b$") || 
               password.startsWith("$2y$");
    }
}