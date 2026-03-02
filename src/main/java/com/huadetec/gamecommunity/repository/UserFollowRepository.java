package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户关注关系数据访问接口
 */
@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    /**
     * 根据关注者和被关注者查询关注关系
     * @param follower 关注者
     * @param following 被关注者
     * @return 关注关系记录（如果存在）
     */
    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);

    /**
     * 检查用户是否关注了另一个用户
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * 根据关注者ID获取关注列表（用户关注了哪些人）
     * @param followerId 关注者ID
     * @return 关注列表
     */
    List<UserFollow> findByFollowerIdOrderByCreateTimeDesc(Long followerId);

    /**
     * 根据被关注者ID获取粉丝列表（哪些人关注了该用户）
     * @param followingId 被关注者ID
     * @return 粉丝列表
     */
    List<UserFollow> findByFollowingIdOrderByCreateTimeDesc(Long followingId);

    /**
     * 获取用户的关注数量
     * @param followerId 关注者ID
     * @return 关注数量
     */
    int countByFollowerId(Long followerId);

    /**
     * 获取用户的粉丝数量
     * @param followingId 被关注者ID
     * @return 粉丝数量
     */
    int countByFollowingId(Long followingId);

    /**
     * 根据关注者和被关注者删除关注关系
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     */
    @Modifying
    @Query("DELETE FROM UserFollow uf WHERE uf.follower.id = :followerId AND uf.following.id = :followingId")
    void deleteByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}