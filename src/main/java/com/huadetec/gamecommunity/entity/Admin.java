package com.huadetec.gamecommunity.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity  // JPA 注解：表示这是数据库实体
@Table(name = "admin_user")  // 关联数据库表名，与user表区分
public class Admin {

    @Id  // 主键
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "admin_seq_gen")  // 使用表生成策略确保与user表id不冲突
    @TableGenerator(
        name = "admin_seq_gen",
        table = "sequence_table",
        pkColumnName = "seq_name",
        valueColumnName = "seq_count",
        pkColumnValue = "admin_seq",
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
    
    // 管理员性别：0-未知，1-男，2-女
    @Column(name = "gender", nullable = false, columnDefinition = "tinyint(1) default 0")
    private Integer gender = 0;
    
    // 管理员年龄
    @Column(name = "age")
    private Integer age;
    
    // 管理员真实姓名
    @Column(name = "real_name", length = 50)
    private String realName;
    
    // 管理员职责描述
    @Column(name = "responsibility", length = 200)
    private String responsibility;
    
    // 管理员最后登录时间
    @Column(name = "last_login")
    private Date lastLogin;
    
    // 管理员在线状态
    @Column(name = "is_online", nullable = false, columnDefinition = "int default 0")
    private Integer isOnline = 0;
    
    // 管理员角色：1-超级管理员，2-普通管理员
    @Column(name = "role", nullable = false, columnDefinition = "int default 2")
    private Integer role = 2;
    
    // 管理员状态：1-启用，0-禁用
    @Column(name = "status", nullable = false, columnDefinition = "int default 1")
    private Integer status = 1;
    
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
    
    public Integer getGender() {
        return gender;
    }
    
    public void setGender(Integer gender) {
        this.gender = gender;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getResponsibility() {
        return responsibility;
    }
    
    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }
    
    public Date getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public Integer getIsOnline() {
        return isOnline;
    }
    
    public void setIsOnline(Integer isOnline) {
        this.isOnline = isOnline;
    }
    
    public Integer getRole() {
        return role;
    }
    
    public void setRole(Integer role) {
        this.role = role;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createTime=" + createTime +
                ", gender=" + gender +
                ", age=" + age +
                ", realName='" + realName + '\'' +
                ", responsibility='" + responsibility + '\'' +
                ", lastLogin=" + lastLogin +
                ", isOnline=" + isOnline +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}