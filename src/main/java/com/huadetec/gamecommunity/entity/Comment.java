package com.huadetec.gamecommunity.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 评论实体类
 * 用于存储用户对攻略的评论
 */
@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 评论ID

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;  // 评论内容

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;  // 评论作者（关联User实体）

    @ManyToOne
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;  // 关联的攻略（关联Guide实体）

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Date createTime;  // 创建时间

    @Column(name = "likes", columnDefinition = "int default 0")
    private Integer likes = 0;  // 点赞数

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Guide getGuide() {
        return guide;
    }

    public void setGuide(Guide guide) {
        this.guide = guide;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", author=" + author.getUsername() +
                ", guideId=" + guide.getId() +
                ", createTime=" + createTime +
                ", likes=" + likes +
                '}';
    }
}