package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.Guide;
import com.huadetec.gamecommunity.entity.GuideLike;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.repository.CommentRepository;
import com.huadetec.gamecommunity.repository.GuideLikeRepository;
import com.huadetec.gamecommunity.repository.GuideRepository;
import com.huadetec.gamecommunity.repository.NotificationRepository;
import com.huadetec.gamecommunity.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 攻略服务类
 * 处理攻略相关的业务逻辑
 */
@Service
public class GuideService {

    @Autowired
    private GuideRepository guideRepository;
    
    @Autowired
    private GuideLikeRepository guideLikeRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * 保存新的攻略
     * @param guide 攻略对象
     * @return 保存后的攻略对象
     */
    public Guide saveGuide(Guide guide) {
        return guideRepository.save(guide);
    }

    /**
     * 根据ID获取攻略
     * @param id 攻略ID
     * @return 攻略对象（如果存在）
     */
    public Optional<Guide> getGuideById(Long id) {
        return guideRepository.findById(id);
    }

    /**
     * 获取所有攻略
     * @return 攻略列表
     */
    public List<Guide> getAllGuides() {
        return guideRepository.findAll();
    }

    /**
     * 根据游戏ID获取攻略列表
     * @param gameId 游戏ID
     * @return 攻略列表
     */
    public List<Guide> getGuidesByGameId(Long gameId) {
        return guideRepository.findByGameIdOrderByCreateTimeDesc(gameId);
    }

    /**
     * 根据用户ID获取攻略列表
     * @param userId 用户ID
     * @return 攻略列表
     */
    public List<Guide> getGuidesByUserId(Long userId) {
        return guideRepository.findByAuthorIdOrderByCreateTimeDesc(userId);
    }

    /**
     * 获取按权重排序的前9个攻略
     * 权重计算公式：likes * 1 + views * 0.1
     * @return 按权重降序排序的前9个攻略列表
     */
    public List<Guide> getTop9GuidesByWeight() {
        return guideRepository.findTop9ByWeightDesc();
    }

    /**
     * 删除攻略
     * @param id 攻略ID
     * @param currentUser 当前登录用户
     * @return 删除结果
     */
    @Transactional
    public boolean deleteGuide(Long id, User currentUser) {
        try {
            // 检查攻略是否存在
            Optional<Guide> guideOptional = guideRepository.findById(id);
            if (!guideOptional.isPresent()) {
                throw new RuntimeException("攻略不存在，ID: " + id);
            }
            
            Guide guide = guideOptional.get();
            
            // 检查权限：只有管理员或攻略作者可以删除
            boolean isAuthor = guide.getAuthor().getId().equals(currentUser.getId());
            
            // 检查当前用户是否有管理员权限（通过SecurityContext获取）
            boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null && 
                             org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                             .anyMatch(authority -> {
                                 String authorityStr = authority.getAuthority();
                                 return "ROLE_ADMIN".equals(authorityStr) || 
                                        "ADMIN".equals(authorityStr) || 
                                        "admin".equals(authorityStr);
                             });
            
            if (!isAdmin && !isAuthor) {
                throw new RuntimeException("没有权限删除此攻略");
            }
            
            // 1. 先删除相关的通知记录（Notification表使用MyISAM引擎，需要手动删除）
            notificationRepository.deleteByRelatedId(id);
            
            // 2. 删除相关的点赞记录（GuideLike表没有设置ON DELETE CASCADE，需要手动删除）
            guideLikeRepository.deleteByGuideId(id);
            
            // 3. 删除相关的评论记录（Comment表没有设置ON DELETE CASCADE，需要手动删除）
            commentRepository.deleteByGuideId(id);
            
            // 4. 发送系统通知给攻略作者
            String notificationContent;
            if (isAdmin && !isAuthor) {
                // 管理员删除攻略，通知作者
                notificationContent = String.format("因违反社区规定，你的攻略《%s》已被删除", guide.getTitle());
                notificationService.createNotificationWithSenderInfo(
                    guide.getAuthor(),
                    currentUser.getId(),
                    "admin",
                    notificationContent,
                    "system",
                    id
                );
            } else if (isAuthor) {
                // 作者自己删除攻略，确认删除操作
                notificationContent = String.format("您已成功删除攻略《%s》", guide.getTitle());
                notificationService.createNotification(
                    guide.getAuthor(),
                    currentUser,
                    notificationContent,
                    "system",
                    id
                );
            } else {
                // 其他情况（理论上不会发生，因为权限检查已通过）
                notificationContent = String.format("攻略《%s》已被删除", guide.getTitle());
                notificationService.createNotification(
                    guide.getAuthor(),
                    currentUser,
                    notificationContent,
                    "system",
                    id
                );
            }
            
            // 5. 最后删除攻略本身
            guideRepository.deleteById(id);
            
            return true;
        } catch (Exception e) {
            // 记录详细的错误信息
            System.err.println("删除攻略失败，ID: " + id + ", 错误信息: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("删除攻略失败，ID: " + id + ", 错误信息: " + e.getMessage());
        }
    }
    
    /**
     * 点赞攻略
     * @param guideId 攻略ID
     * @param user 当前登录用户
     * @return 点赞后的点赞数
     */
    public int likeGuide(Long guideId, User user) {
        Optional<Guide> guideOptional = guideRepository.findById(guideId);
        if (guideOptional.isPresent()) {
            Guide guide = guideOptional.get();
            
            // 检查用户是否已经点赞过这篇攻略
            if (guideLikeRepository.existsByUserAndGuide(user, guide)) {
                throw new RuntimeException("您已经赞过此攻略");
            }
            
            // 创建新的点赞记录
            GuideLike guideLike = new GuideLike();
            guideLike.setUser(user);
            guideLike.setGuide(guide);
            guideLikeRepository.save(guideLike);
            
            // 更新攻略的点赞数
            Integer currentLikes = guide.getLikes();
            if (currentLikes == null) {
                currentLikes = 0;
            }
            guide.setLikes(currentLikes + 1);
            guideRepository.save(guide);
            
            return guide.getLikes();
        }
        throw new RuntimeException("攻略不存在");
    }
    
    /**
     * 检查用户是否已经点赞了指定攻略
     * @param guideId 攻略ID
     * @param user 当前登录用户
     * @return 是否已点赞
     */
    public boolean checkUserLikedGuide(Long guideId, User user) {
        if (user == null) {
            return false;
        }
        
        Optional<Guide> guideOptional = guideRepository.findById(guideId);
        if (guideOptional.isPresent()) {
            return guideLikeRepository.existsByUserAndGuide(user, guideOptional.get());
        }
        return false;
    }
    
    /**
     * 获取攻略的点赞数
     * @param guideId 攻略ID
     * @return 点赞数
     */
    public int getGuideLikesCount(Long guideId) {
        return guideLikeRepository.countByGuideId(guideId);
    }

    /**
     * 更新攻略信息
     * @param guideId 攻略ID
     * @param title 新标题
     * @param content 新内容
     * @param coverImageUrl 新封面图片URL
     * @param gameId 新游戏ID
     * @param currentUser 当前登录用户
     * @return 更新后的攻略对象
     */
    @Transactional
    public Guide updateGuide(Long guideId, String title, String content, String coverImageUrl, Long gameId, User currentUser) {
        // 检查攻略是否存在
        Optional<Guide> guideOptional = guideRepository.findById(guideId);
        if (!guideOptional.isPresent()) {
            throw new RuntimeException("攻略不存在，ID: " + guideId);
        }

        Guide guide = guideOptional.get();
        
        // 检查权限：只有管理员或攻略作者可以编辑
        boolean isAuthor = guide.getAuthor().getId().equals(currentUser.getId());
        
        // 检查当前用户是否有管理员权限
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null && 
                         org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                         .anyMatch(authority -> {
                             String authorityStr = authority.getAuthority();
                             return "ROLE_ADMIN".equals(authorityStr) || 
                                    "ADMIN".equals(authorityStr) || 
                                    "admin".equals(authorityStr);
                         });
        
        if (!isAdmin && !isAuthor) {
            throw new RuntimeException("没有权限编辑此攻略");
        }

        // 验证参数
        if (title == null || title.trim().isEmpty() || title.length() > 200) {
            throw new RuntimeException("标题不能为空且不能超过200个字符");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("攻略内容不能为空");
        }

        // 更新攻略信息
        guide.setTitle(title);
        guide.setContent(content);
        
        // 处理封面图片URL
        if (coverImageUrl != null) {
            guide.setCoverImageUrl(coverImageUrl);
        }
        
        // 如果提供了新的游戏ID，更新游戏信息
        if (gameId != null) {
            // 这里需要注入GameRepository来查找游戏
            // 由于GuideService中没有GameRepository，我们将在Controller中处理游戏更新
            // 这个方法只负责更新攻略的基本信息
        }

        // 保存更新后的攻略
        Guide updatedGuide = guideRepository.save(guide);

        // 发送系统通知给攻略作者（如果是管理员编辑）
        if (isAdmin && !isAuthor) {
            String notificationContent = String.format("管理员已编辑您的攻略《%s》", title);
            notificationService.createNotificationWithSenderInfo(
                guide.getAuthor(),
                currentUser.getId(),
                "admin",
                notificationContent,
                "system",
                guideId
            );
        }

        return updatedGuide;
    }
}