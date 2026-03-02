package com.huadetec.gamecommunity.util;

import com.huadetec.gamecommunity.entity.Admin;
import com.huadetec.gamecommunity.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 管理员用户表初始化工具
 * 在应用启动时检查是否存在管理员用户，如果不存在则创建默认管理员用户到admin_user表
 */
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 创建硬编码管理员用户
        logger.info("开始初始化管理员用户...");
        
        // 创建或更新admin2用户
        String password = "admin2";
        if (!adminService.existsByUsername("admin2")) {
            adminService.register("admin2", passwordEncoder.encode(password), "admin2@example.com");
            logger.info("创建管理员用户: admin2");
        } else {
            // 直接更新密码，不验证旧密码
            adminService.updatePassword("admin2", password);
            logger.info("更新管理员用户admin2的密码");
        }
        
        // 创建或更新admin用户
        if (!adminService.existsByUsername("admin")) {
            adminService.register("admin", passwordEncoder.encode(password), "admin@example.com");
            logger.info("创建管理员用户: admin");
        } else {
            // 直接更新密码，不验证旧密码
            adminService.updatePassword("admin", password);
            logger.info("更新管理员用户admin的密码");
        }
        
        // 创建或更新admin1用户
        if (!adminService.existsByUsername("admin1")) {
            adminService.register("admin1", passwordEncoder.encode(password), "admin1@example.com");
            logger.info("创建管理员用户: admin1");
        } else {
            // 直接更新密码，不验证旧密码
            adminService.updatePassword("admin1", password);
            logger.info("更新管理员用户admin1的密码");
        }
        
        logger.info("管理员用户初始化完成");
    }
}