package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 攻略数据访问接口
 */
@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {

    /**
     * 根据游戏ID查询攻略列表
     * @param gameId 游戏ID
     * @return 攻略列表
     */
    List<Guide> findByGameIdOrderByCreateTimeDesc(Long gameId);

    /**
     * 根据用户ID查询攻略列表（用户发布的所有攻略）
     * @param userId 用户ID
     * @return 攻略列表
     */
    List<Guide> findByAuthorIdOrderByCreateTimeDesc(Long userId);

    /**
     * 按权重排序获取前9个攻略
     * 权重计算公式：likes * 1 + views * 0.1
     * @return 按权重降序排序的前9个攻略列表
     */
    @Query(value = "SELECT * FROM guide ORDER BY (likes * 1 + views * 0.1) DESC LIMIT 9", nativeQuery = true)
    List<Guide> findTop9ByWeightDesc();
}