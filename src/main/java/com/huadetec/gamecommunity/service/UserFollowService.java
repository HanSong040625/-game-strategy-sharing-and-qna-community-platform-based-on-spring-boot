package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.UserFollow;
import java.util.List;

/**
 * 用户关注关系服务接口
 */
public interface UserFollowService {

    /**
     * 保存关注关系
     * @param userFollow 关注关系对象
     * @return 保存后的关注关系对象
     */
    UserFollow save(UserFollow userFollow);

    /**
     * 取消关注
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     */
    void unfollow(Long followerId, Long followingId);

    /**
     * 检查是否关注了某个用户
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取用户的关注列表
     * @param followerId 关注者ID
     * @return 关注列表
     */
    List<UserFollow> getFollowingList(Long followerId);

    /**
     * 获取用户的粉丝列表
     * @param followingId 被关注者ID
     * @return 粉丝列表
     */
    List<UserFollow> getFollowersList(Long followingId);

    /**
     * 获取用户的关注数量
     * @param followerId 关注者ID
     * @return 关注数量
     */
    int getFollowingCount(Long followerId);

    /**
     * 获取用户的粉丝数量
     * @param followingId 被关注者ID
     * @return 粉丝数量
     */
    int getFollowersCount(Long followingId);
}