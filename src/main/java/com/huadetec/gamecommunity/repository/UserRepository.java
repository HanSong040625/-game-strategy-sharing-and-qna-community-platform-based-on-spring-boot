package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository  // 标记为数据访问层组件
// JpaRepository<实体类, 主键类型>：自带 CRUD 方法（save、findById 等）
public interface UserRepository extends JpaRepository<User, Long> {

    // 自定义查询：根据用户名找用户（登录时用）
    Optional<User> findByUsername(String username);

    // 自定义查询：判断用户名是否已存在（注册时用）
    boolean existsByUsername(String username);

    // 自定义查询：判断邮箱是否已存在（注册时用）
    boolean existsByEmail(String email);
}