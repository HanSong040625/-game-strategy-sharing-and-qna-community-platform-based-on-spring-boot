package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.entity.Guide;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.entity.UserFollow;
import com.huadetec.gamecommunity.service.GuideService;
import com.huadetec.gamecommunity.service.UserFollowService;
import com.huadetec.gamecommunity.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.huadetec.gamecommunity.util.Result;

/**
 * 用户关注关系控制器
 */
@RestController
@RequestMapping("/api/follow")
public class UserFollowController {

    private static final Logger logger = LoggerFactory.getLogger(UserFollowController.class);

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private UserService userService;

    @Autowired
    private GuideService guideService;

    /**
     * 关注用户
     */
    @PostMapping("/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<?>> followUser(@RequestParam Long followingId) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || (authentication.getPrincipal() instanceof String)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未登录"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String currentUsername = userDetails.getUsername();

            // 获取当前用户
            User currentUser = userService.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("当前用户不存在"));

            // 获取被关注用户
            User followingUser = userService.findById(followingId)
                    .orElseThrow(() -> new RuntimeException("被关注用户不存在"));

            // 不能关注自己
            if (currentUser.getId().equals(followingId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "不能关注自己"));
            }

            // 检查是否已经关注
            if (userFollowService.isFollowing(currentUser.getId(), followingId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "已经关注该用户"));
            }

            // 创建关注关系
            UserFollow userFollow = new UserFollow();
            userFollow.setFollower(currentUser);
            userFollow.setFollowing(followingUser);
            userFollow.setCreateTime(new Date());

            userFollowService.save(userFollow);

            logger.info("用户 {} 关注了用户 {}", currentUsername, followingUser.getUsername());

            return ResponseEntity.ok(Result.success(null));
        } catch (Exception e) {
            logger.error("关注用户失败：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "关注失败：" + e.getMessage()));
        }
    }

    /**
     * 取消关注
     */
    @PostMapping("/unfollow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<?>> unfollowUser(@RequestParam Long followingId) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || (authentication.getPrincipal() instanceof String)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未登录"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String currentUsername = userDetails.getUsername();

            // 获取当前用户
            User currentUser = userService.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("当前用户不存在"));

            // 取消关注
            userFollowService.unfollow(currentUser.getId(), followingId);

            logger.info("用户 {} 取消关注了用户 {}", currentUsername, followingId);

            return ResponseEntity.ok(Result.success(null));
        } catch (Exception e) {
            logger.error("取消关注失败：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "取消关注失败：" + e.getMessage()));
        }
    }

    /**
     * 检查是否关注了某个用户
     */
    @GetMapping("/is-following")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<Map<String, Boolean>>> isFollowing(@RequestParam Long followingId) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || (authentication.getPrincipal() instanceof String)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未登录"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String currentUsername = userDetails.getUsername();

            // 获取当前用户
            User currentUser = userService.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("当前用户不存在"));

            boolean isFollowing = userFollowService.isFollowing(currentUser.getId(), followingId);

            Map<String, Boolean> data = new HashMap<>();
            data.put("isFollowing", isFollowing);

            return ResponseEntity.ok(Result.success(data));
        } catch (Exception e) {
            logger.error("检查关注状态失败：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "检查关注状态失败"));
        }
    }

    /**
     * 获取用户的关注列表
     */
    @GetMapping("/following")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result<List<Map<String, Object>>>> getFollowingList(@RequestParam Long userId) {
        try {
            List<UserFollow> followingList = userFollowService.getFollowingList(userId);

            // 构建响应数据
            List<Map<String, Object>> data = new ArrayList<>();
            for (UserFollow follow : followingList) {
                User followingUser = follow.getFollowing();
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", followingUser.getId());
                userData.put("username", followingUser.getUsername());
                userData.put("avatar", followingUser.getAvatarUrl() != null ? followingUser.getAvatarUrl() : "");
                userData.put("followTime", follow.getCreateTime());
                data.add(userData);
            }

            return ResponseEntity.ok(Result.success(data));
        } catch (Exception e) {
            logger.error("获取关注列表失败：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取关注列表失败"));
        }
    }

    /**
     * 获取用户的粉丝列表
     */
    @GetMapping("/followers")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result<List<Map<String, Object>>>> getFollowersList(@RequestParam Long userId) {
        try {
            List<UserFollow> followersList = userFollowService.getFollowersList(userId);

            // 构建响应数据
            List<Map<String, Object>> data = new ArrayList<>();
            for (UserFollow follow : followersList) {
                User followerUser = follow.getFollower();
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", followerUser.getId());
                userData.put("username", followerUser.getUsername());
                userData.put("avatar", followerUser.getAvatarUrl() != null ? followerUser.getAvatarUrl() : "");
                userData.put("followTime", follow.getCreateTime());
                data.add(userData);
            }

            return ResponseEntity.ok(Result.success(data));
        } catch (Exception e) {
            logger.error("获取粉丝列表失败：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取粉丝列表失败"));
        }
    }

    /**
     * 获取用户的关注数量和粉丝数量
     */
    @GetMapping("/stats")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result<Map<String, Integer>>> getUserFollowStats(@RequestParam Long userId) {
        try {
            int followingCount = userFollowService.getFollowingCount(userId);
            int followersCount = userFollowService.getFollowersCount(userId);

            Map<String, Integer> data = new HashMap<>();
            data.put("followingCount", followingCount);
            data.put("followersCount", followersCount);

            return ResponseEntity.ok(Result.success(data));
        } catch (Exception e) {
            logger.error("获取关注统计失败：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取关注统计失败"));
        }
    }

    /**
     * 获取用户发布的攻略列表
     */
    @GetMapping("/user/{userId}/guides")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result<List<Map<String, Object>>>> getUserGuides(@PathVariable Long userId) {
        try {
            // 检查用户是否存在
            if (!userService.findById(userId).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.error(404, "用户不存在"));
            }

            // 获取用户发布的攻略列表
            List<Guide> guides = guideService.getGuidesByUserId(userId);

            // 构建响应数据
            List<Map<String, Object>> data = new ArrayList<>();
            for (Guide guide : guides) {
                Map<String, Object> guideData = new HashMap<>();
                guideData.put("id", guide.getId());
                guideData.put("title", guide.getTitle());
                guideData.put("content", guide.getContent() != null ? guide.getContent().substring(0, Math.min(100, guide.getContent().length())) : "");
                guideData.put("coverImageUrl", guide.getCoverImageUrl() != null ? guide.getCoverImageUrl() : "");
                guideData.put("views", guide.getViews() != null ? guide.getViews() : 0);
                guideData.put("likes", guide.getLikes() != null ? guide.getLikes() : 0);
                guideData.put("createTime", guide.getCreateTime() != null ? guide.getCreateTime() : "");
                
                Map<String, Object> gameData = new HashMap<>();
                gameData.put("id", guide.getGame() != null ? guide.getGame().getId() : 0);
                gameData.put("name", guide.getGame() != null ? guide.getGame().getName() : "");
                guideData.put("game", gameData);
                
                data.add(guideData);
            }

            return ResponseEntity.ok(Result.success(data));
        } catch (Exception e) {
            logger.error("获取用户攻略列表失败：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取用户攻略列表失败"));
        }
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}