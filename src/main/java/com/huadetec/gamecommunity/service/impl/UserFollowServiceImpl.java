package com.huadetec.gamecommunity.service.impl;

import com.huadetec.gamecommunity.entity.UserFollow;
import com.huadetec.gamecommunity.repository.UserFollowRepository;
import com.huadetec.gamecommunity.service.UserFollowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户关注关系服务实现类
 */
@Service
public class UserFollowServiceImpl implements UserFollowService {

    private static final Logger logger = LoggerFactory.getLogger(UserFollowServiceImpl.class);

    @Autowired
    private UserFollowRepository userFollowRepository;

    /**
     * 保存关注关系
     */
    @Override
    @Transactional
    public UserFollow save(UserFollow userFollow) {
        try {
            logger.info("保存关注关系：关注者ID={}, 被关注者ID={}", 
                    userFollow.getFollower().getId(), userFollow.getFollowing().getId());
            
            UserFollow savedFollow = userFollowRepository.save(userFollow);
            
            logger.info("关注关系保存成功：ID={}", savedFollow.getId());
            return savedFollow;
        } catch (Exception e) {
            logger.error("保存关注关系失败：关注者ID={}, 被关注者ID={}, 错误信息={}", 
                    userFollow.getFollower().getId(), userFollow.getFollowing().getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 取消关注
     */
    @Override
    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        try {
            logger.info("取消关注：关注者ID={}, 被关注者ID={}", followerId, followingId);
            
            // 检查是否存在关注关系
            if (!userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
                logger.warn("取消关注失败：关注关系不存在，关注者ID={}, 被关注者ID={}", followerId, followingId);
                throw new RuntimeException("关注关系不存在");
            }
            
            // 删除关注关系
            userFollowRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
            
            logger.info("取消关注成功：关注者ID={}, 被关注者ID={}", followerId, followingId);
        } catch (Exception e) {
            logger.error("取消关注失败：关注者ID={}, 被关注者ID={}, 错误信息={}", 
                    followerId, followingId, e.getMessage());
            throw e;
        }
    }

    /**
     * 检查是否关注了某个用户
     */
    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        try {
            boolean exists = userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
            logger.debug("检查关注状态：关注者ID={}, 被关注者ID={}, 是否关注={}", 
                    followerId, followingId, exists);
            return exists;
        } catch (Exception e) {
            logger.error("检查关注状态失败：关注者ID={}, 被关注者ID={}, 错误信息={}", 
                    followerId, followingId, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取用户的关注列表
     */
    @Override
    public List<UserFollow> getFollowingList(Long followerId) {
        try {
            List<UserFollow> followingList = userFollowRepository.findByFollowerIdOrderByCreateTimeDesc(followerId);
            logger.info("获取关注列表：关注者ID={}, 关注数量={}", followerId, followingList.size());
            return followingList;
        } catch (Exception e) {
            logger.error("获取关注列表失败：关注者ID={}, 错误信息={}", followerId, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取用户的粉丝列表
     */
    @Override
    public List<UserFollow> getFollowersList(Long followingId) {
        try {
            List<UserFollow> followersList = userFollowRepository.findByFollowingIdOrderByCreateTimeDesc(followingId);
            logger.info("获取粉丝列表：被关注者ID={}, 粉丝数量={}", followingId, followersList.size());
            return followersList;
        } catch (Exception e) {
            logger.error("获取粉丝列表失败：被关注者ID={}, 错误信息={}", followingId, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取用户的关注数量
     */
    @Override
    public int getFollowingCount(Long followerId) {
        try {
            int count = userFollowRepository.countByFollowerId(followerId);
            logger.debug("获取关注数量：关注者ID={}, 关注数量={}", followerId, count);
            return count;
        } catch (Exception e) {
            logger.error("获取关注数量失败：关注者ID={}, 错误信息={}", followerId, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取用户的粉丝数量
     */
    @Override
    public int getFollowersCount(Long followingId) {
        try {
            int count = userFollowRepository.countByFollowingId(followingId);
            logger.debug("获取粉丝数量：被关注者ID={}, 粉丝数量={}", followingId, count);
            return count;
        } catch (Exception e) {
            logger.error("获取粉丝数量失败：被关注者ID={}, 错误信息={}", followingId, e.getMessage());
            throw e;
        }
    }
}