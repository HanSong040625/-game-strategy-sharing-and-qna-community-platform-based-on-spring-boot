package com.huadetec.gamecommunity.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 用户关注关系实体类
 * 记录用户之间的关注关系
 */
@Entity
@Table(name = "user_follow", uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;  // 关注者（粉丝）

    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    private User following;  // 被关注者

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowing() {
        return following;
    }

    public void setFollowing(User following) {
        this.following = following;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserFollow{" +
                "id=" + id +
                ", follower=" + (follower != null ? follower.getUsername() : "null") +
                ", following=" + (following != null ? following.getUsername() : "null") +
                ", createTime=" + createTime +
                '}';
    }
}