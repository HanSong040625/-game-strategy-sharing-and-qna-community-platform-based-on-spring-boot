package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.GuideLike;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.entity.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 攻略点赞数据访问接口
 */
@Repository
public interface GuideLikeRepository extends JpaRepository<GuideLike, Long> {

    /**
     * 根据用户和攻略查询点赞记录
     * @param user 用户
     * @param guide 攻略
     * @return 点赞记录（如果存在）
     */
    Optional<GuideLike> findByUserAndGuide(User user, Guide guide);

    /**
     * 根据攻略ID查询点赞数量
     * @param guideId 攻略ID
     * @return 点赞数量
     */
    int countByGuideId(Long guideId);

    /**
     * 检查用户是否已点赞指定攻略
     * @param user 用户
     * @param guide 攻略
     * @return 是否已点赞
     */
    boolean existsByUserAndGuide(User user, Guide guide);
    
    /**
     * 根据攻略ID删除所有相关点赞记录
     * @param guideId 攻略ID
     */
    @Modifying
    @Query(value = "DELETE FROM guide_like WHERE guide_id = :guideId", nativeQuery = true)
    void deleteByGuideId(Long guideId);
}