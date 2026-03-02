package com.huadetec.gamecommunity.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity  // JPA 注解：表示这是数据库实体
@Table(name = "user")  // 关联数据库表名
public class User {

    @Id  // 主键
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "user_seq_gen")  // 使用表生成策略确保与admin_user表id不冲突
    @TableGenerator(
        name = "user_seq_gen",
        table = "sequence_table",
        pkColumnName = "seq_name",
        valueColumnName = "seq_count",
        pkColumnValue = "user_seq",
        allocationSize = 1
    )
    private Long id;

    @Column(unique = true, nullable = false)  // 唯一且非空
    private String username;  // 用户名

    @Column(nullable = false)
    private String password;  // 加密后的密码

    @Column(unique = true, nullable = false)
    private String email;  // 邮箱

    @CreationTimestamp  // 自动填充创建时间
    @Column(name = "create_time", updatable = false)
    private Date createTime;
    
    // 最后登录时间
    @Column(name = "last_login")
    private Date lastLogin;
    
    // 用户状态（0:正常，1:禁用）
    @Column(name = "is_banned", nullable = false, columnDefinition = "int default 0")
    private Integer isBanned = 0;
    
    // 用户头像URL
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    // 是否为管理员：0表示普通用户，1表示管理员
    @Column(name = "is_admin", nullable = false, columnDefinition = "tinyint(1) default 0")
    private Integer isAdmin = 0;
    

    

    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Date getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public Integer getIsBanned() {
        return isBanned;
    }
    
    public void setIsBanned(Integer isBanned) {
        this.isBanned = isBanned;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public Integer getIsAdmin() {
        return isAdmin;
    }
    
    public void setIsAdmin(Integer isAdmin) {
        this.isAdmin = isAdmin;
    }
    

    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createTime=" + createTime +
                ", lastLogin=" + lastLogin +
                ", isBanned=" + isBanned +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}