package com.huadetec.gamecommunity.util;

import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * 管理员账号初始化工具
 * 在应用启动时检查是否存在管理员账号，如果不存在则创建一个默认管理员账号
 */
// @Component
// 暂时禁用此类，避免与AdminUserInitializer冲突
public class AdminInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    // 默认管理员用户名
    private static final String ADMIN_USERNAME = "admin";
    // 默认管理员密码
    private static final String ADMIN_PASSWORD = "admin123";
    // 默认管理员邮箱
    private static final String ADMIN_EMAIL = "admin@example.com";

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 检查是否已经存在管理员账号
        Optional<User> adminUserOptional = userRepository.findByUsername(ADMIN_USERNAME);

        if (!adminUserOptional.isPresent()) {
            // 创建新的管理员账号
            User adminUser = new User();
            adminUser.setUsername(ADMIN_USERNAME);
            adminUser.setPassword(ADMIN_PASSWORD); // 注意：实际项目中应该加密密码
            adminUser.setEmail(ADMIN_EMAIL);
            adminUser.setCreateTime(new Date());

            // 保存管理员账号到数据库
            userRepository.save(adminUser);
            logger.info("默认管理员账号已创建: 用户名={}, 密码={}, 邮箱={}", 
                        ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL);
        } else {
            // 检查现有管理员账号
            User adminUser = adminUserOptional.get();
            // 更新密码为默认值
            adminUser.setPassword(ADMIN_PASSWORD); // 确保密码为默认值
            userRepository.save(adminUser);
            logger.info("现有用户 '{}' 已更新为管理员角色，密码已重置为默认值", ADMIN_USERNAME);
        }
    }
}