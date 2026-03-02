package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 游戏数据访问接口
 * 提供对game表的CRUD操作和自定义查询方法
 */
@Repository  // 标记为数据访问层组件
// JpaRepository<实体类, 主键类型>：自带 CRUD 方法（save、findById 等）
public interface GameRepository extends JpaRepository<Game, Long> {

    // 自定义查询：根据游戏名称查找游戏
    Optional<Game> findByName(String name);

    // 自定义查询：查找推荐游戏（首页大图展示）
    List<Game> findByIsFeaturedTrue();

    // 自定义查询：根据分类查找游戏
    List<Game> findByCategoriesContaining(String category);

    // 自定义查询：判断游戏名称是否已存在
    boolean existsByName(String name);
}