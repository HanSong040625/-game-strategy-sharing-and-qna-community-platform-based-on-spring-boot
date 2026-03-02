package com.huadetec.gamecommunity.entity;
import org.hibernate.annotations.CreationTimestamp;//自动生成创建时间
import javax.persistence.*;//JPA（Java 持久化 API）的核心注解包，用于对象 - 关系映射（ORM）。
import java.util.Date;//Date：Java 的日期类型，用于存储时间。
/**
 * 攻略实体类
 * 用于存储用户发布的游戏攻略
 */
@Entity //JPA 注解，标识当前类是 “实体类”
@Table(name = "guide") //JPA 注解，指定当前实体类对应的数据库表名是guide
public class Guide {

    @Id //JPA 注解，标识当前字段是表的 “主键”
    @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 注解，指定主键生成策略。IDENTITY表示主键由数据库自动生成（如 MySQL 的自增主键），无需手动赋值
    private Long id;  // 攻略ID

    @Column(name = "title", nullable = false, length = 200)
    private String title;  // 攻略标题

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;  // 攻略内容

    @Column(name = "cover_image_url")
    private String coverImageUrl;  // 攻略封面图片URL

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;  // 攻略作者（关联User实体）

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;  // 关联的游戏（关联Game实体）

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Date createTime;  // 创建时间

    @Column(name = "views", columnDefinition = "int default 0")
    private Integer views = 0;  // 浏览量

    @Column(name = "likes", columnDefinition = "int default 0")
    private Integer likes = 0;  // 点赞数

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Integer getViews() {
        return views;
    }
    
    public void setViews(Integer views) {
        this.views = views;
    }
    
    public Integer getLikes() {
        return likes;
    }
    
    public void setLikes(Integer likes) {
        this.likes = likes;
    }
}