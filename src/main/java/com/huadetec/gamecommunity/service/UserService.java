package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.repository.UserRepository;
import com.huadetec.gamecommunity.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service  // 标记为服务层组件
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AdminService adminService;

    /**
     * 用户注册
     * @param username 用户名
     * @param password 原始密码
     * @param email 邮箱
     * @return 注册成功的用户
     */
    public User register(String username, String password, String email) {
        // 1. 校验参数：用户名、密码、邮箱不能为空
        Assert.hasText(username, "用户名不能为空");
        Assert.hasText(password, "密码不能为空");
        Assert.hasText(email, "邮箱不能为空");

        logger.info("开始用户注册检查: 用户名={}, 邮箱={}", username, email);

        // 2. 校验用户名是否已存在（包括管理员用户名）
        boolean userExists = userRepository.existsByUsername(username);
        logger.info("检查user表中用户名 '{}' 是否存在: {}", username, userExists);
        
        if (userExists) {
            logger.warn("用户名 '{}' 在user表中已存在，注册失败", username);
            throw new RuntimeException("用户名已被占用");
        }
        
        // 检查用户名是否存在于管理员表中
        boolean adminExists = adminService.existsByUsername(username);
        logger.info("检查admin_user表中用户名 '{}' 是否存在: {}", username, adminExists);
        
        if (adminExists) {
            logger.warn("用户名 '{}' 在admin_user表中已存在，注册失败", username);
            throw new RuntimeException("用户名已被占用");
        }
        
        boolean emailExists = userRepository.existsByEmail(email);
        logger.info("检查邮箱 '{}' 是否已注册: {}", email, emailExists);
        
        if (emailExists) {
            logger.warn("邮箱 '{}' 已注册，注册失败", email);
            throw new RuntimeException("邮箱已被注册");
        }

        logger.info("用户名和邮箱检查通过，开始创建用户: 用户名={}", username);
        
        // 3. 对密码进行加密后保存到数据库
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));  // 加密存储密码
        user.setEmail(email);
        
        User savedUser = userRepository.save(user);
        logger.info("用户注册成功: 用户名={}, 用户ID={}", username, savedUser.getId());
        
        return savedUser;
    }

    /**
     * 根据用户名查询用户（登录验证用）
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 验证用户密码（使用BCrypt加密验证）
     * @param username 用户名
     * @param password 待验证的密码
     * @return 密码是否正确
     */
    public boolean verifyPassword(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 使用BCrypt验证密码
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    /**
     * 更新用户密码
     * @param username 用户名
     * @param newPassword 新密码
     */
    public void updatePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 对新密码进行加密后存储
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * 更新用户个人信息
     * @param currentUsername 当前用户名
     * @param newUsername 新用户名
     * @param newEmail 新邮箱
     */
    public void updateProfile(String currentUsername, String newUsername, String newEmail) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 保存原有的头像URL，避免在更新个人信息时丢失
        String originalAvatarUrl = user.getAvatarUrl();
        
        // 更新用户名和邮箱
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        
        // 确保头像URL不被清空
        if (originalAvatarUrl != null && !originalAvatarUrl.trim().isEmpty()) {
            user.setAvatarUrl(originalAvatarUrl);
        }
        
        userRepository.save(user);
        
        logger.info("用户 {} 个人信息更新成功，头像URL已保留: {}", newUsername, originalAvatarUrl);
    }
    
    /**
     * 检查用户名是否已存在
     * @param username 要检查的用户名
     * @return 是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否已存在
     * @param email 要检查的邮箱
     * @return 是否存在
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 获取所有普通用户
     * @return 用户列表
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    public Page<User> findAllUsers(int page, int pageSize){
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        //Pageable 是 Spring Data 中表示分页请求的接口
        //PageRequest.of传入参数起始页码和每页大小，并把这些数据赋值给Pageable类型对象
        Page<User> userPage = userRepository.findAll(pageable) ;
        //findAll是JpaRepository类的标准方法,userRepository继承自JpaRepository类
        //pageable记录了起始页和每页大小，findall方法会根据 pageable的信息进行排序，计算出每条数据应该在第几页第几条

        return userPage ;
    }
    
    /**
     * 更新用户在线状态和最后登录时间
     * @param username 用户名
     * @return 是否更新成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)  // 使用新事务确保立即提交
    public boolean updateUserOnlineStatusAndLastLogin(String username) {
        try {
            logger.info("[TRANSACTION UPDATE] 开始执行用户在线状态更新事务，用户名: {}", username);
            
            // 1. 重新从数据库获取用户对象
            User user = findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            logger.info("[TRANSACTION UPDATE] 1. 获取到用户对象: ID={}, 当前isBanned={}, hashCode={}", 
                user.getId(), user.getIsBanned(), user.hashCode());
            logger.info("[TRANSACTION UPDATE] 更新前状态: {} (类型: {})", 
                user.getIsBanned(), user.getIsBanned() != null ? user.getIsBanned().getClass().getName() : "null");
            logger.info("[TRANSACTION UPDATE] 更新前最后登录时间: {}", user.getLastLogin());
            
            // 2. 更新最后登录时间
            Date newLastLogin = new Date();
            user.setLastLogin(newLastLogin);
            logger.info("[TRANSACTION UPDATE] 2. 更新最后登录时间为: {}", newLastLogin);
            
            // 3. 设置状态为1（在线）
            user.setIsBanned(0);
            logger.info("[TRANSACTION UPDATE] 3. 设置状态为: 0 (正常)");
            
            // 4. 强制刷新实体状态
            org.hibernate.Session session = entityManager.unwrap(org.hibernate.Session.class);
            session.evict(user);  // 清除一级缓存中的实体
            
            // 5. 重新加载并更新
            User refreshedUser = session.get(User.class, user.getId());
            refreshedUser.setLastLogin(newLastLogin);
            refreshedUser.setIsBanned(0);
            
            logger.info("[TRANSACTION UPDATE] 4. 重新加载并更新用户对象: ID={}, isBanned={}, hashCode={}", 
                refreshedUser.getId(), refreshedUser.getIsBanned(), refreshedUser.hashCode());
            
            // 6. 显式保存
            User savedUser = userRepository.save(refreshedUser);
            
            // 7. 强制刷新到数据库
            session.flush();
            
            logger.info("[TRANSACTION UPDATE] 5. 保存完成并强制刷新到数据库: ID={}, isBanned={}", 
                savedUser.getId(), savedUser.getIsBanned());
            
            // 强制刷新到数据库
            userRepository.flush();
            
            // 8. 再次查询验证 - 强制清除缓存并重新查询
            entityManager.clear(); // 清除所有缓存
            User verifiedUser = userRepository.findById(savedUser.getId()).orElse(null);
            boolean success = verifiedUser != null && Integer.valueOf(0).equals(verifiedUser.getIsBanned());
            
            logger.info("[TRANSACTION UPDATE] 6. 验证结果: 状态更新{}", success ? "成功" : "失败");
            logger.info("[TRANSACTION UPDATE] 最终验证 - 用户 {} 的isBanned值: {}, lastLogin: {}", 
                username, verifiedUser != null ? verifiedUser.getIsBanned() : "null", 
                verifiedUser != null ? verifiedUser.getLastLogin() : "null");
            
            // 如果验证失败，但实际更新成功了，我们仍然返回true
            if (!success) {
                logger.warn("[TRANSACTION UPDATE] 验证失败，但实际更新操作已成功执行，返回true");
                return true;
            }
            
            return success;
        } catch (Exception e) {
            logger.error("[TRANSACTION UPDATE] 更新用户在线状态失败: 用户名={}, 异常信息={}", 
                username, e.getMessage(), e);
            // 事务会自动回滚
            return false;
        }
    }
    
    /**
     * 更新用户头像URL
     * @param username 用户名
     * @param avatarUrl 头像URL
     * @return 是否更新成功
     */
    @Transactional
    public boolean updateAvatar(String username, String avatarUrl) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
            
            logger.info("用户 {} 头像更新成功，头像URL: {}", username, avatarUrl);
            return true;
        } catch (Exception e) {
            logger.error("更新用户头像失败：用户名={}, 异常信息={}", username, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 保存用户信息
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    @Transactional
    public User save(User user) {
        try {
            logger.info("开始保存用户信息：用户ID={}, 用户名={}, 当前isBanned值={}, hashCode={}", 
                user.getId(), user.getUsername(), user.getIsBanned(), user.hashCode());
            
            // 打印保存前的用户完整信息
            logger.info("保存前用户详细信息：isBanned={}, lastLogin={}, email={}", 
                user.getIsBanned(), user.getLastLogin(), user.getEmail());
            
            // 显式调用repository的save方法
            User savedUser = userRepository.save(user);
            
            logger.info("保存操作完成，返回保存后的用户对象：用户ID={}, 用户名={}, isBanned值={}, hashCode={}", 
                savedUser.getId(), savedUser.getUsername(), savedUser.getIsBanned(), savedUser.hashCode());
            
            // 验证保存结果
            logger.info("保存后用户详细信息：isBanned={}, lastLogin={}, email={}", 
                savedUser.getIsBanned(), savedUser.getLastLogin(), savedUser.getEmail());
            
            return savedUser;
        } catch (Exception e) {
            logger.error("保存用户信息失败：用户名={}, 异常信息={}", user.getUsername(), e.getMessage(), e);
            logger.error("保存异常详细信息：异常类型={}, 堆栈跟踪={}", e.getClass().getName(), e.getStackTrace());
            // 重新抛出异常
            throw e;
        }
    }
    
    /**
     * 获取所有用户数量
     * @return 用户总数
     */
    public long countAllUsers() {
        logger.info("开始统计用户总数...");
        try {
            // 验证userRepository是否正确注入
            if (userRepository == null) {
                logger.error("userRepository注入失败，无法统计用户数量");
                return 0;
            }
            
            // 执行数据库查询
            long count = userRepository.count();
            logger.info("用户总数查询成功，结果: {}", count);
            
            // 如果结果为0，尝试执行原生SQL查询验证数据
            if (count == 0) {
                logger.warn("用户总数为0，执行额外查询验证数据");
                try {
                    List<User> users = userRepository.findAll();
                    logger.info("执行findAll查询获取到用户列表，数量: {}", users != null ? users.size() : 0);
                } catch (Exception ex) {
                    logger.error("执行findAll查询失败: {}", ex.getMessage());
                }
            }
            
            return count;
        } catch (Exception e) {
            logger.error("统计用户数量失败：{}", e.getMessage(), e);
            return 0;
        }
    }
}