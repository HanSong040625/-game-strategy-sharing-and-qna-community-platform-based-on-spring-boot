package com.huadetec.gamecommunity.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * 游戏实体类
 * 用于存储游戏专区的信息
 */
@Entity  // 标记为JPA实体
@Table(name = "game")  // 映射到game表
public class Game {

    @Id  // 主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自增策略
    private Long id;  // 游戏ID

    @Column(name = "name", nullable = false, length = 100)  // 游戏名称，非空，最大长度100
    private String name;

    @Column(name = "description", length = 500)  // 游戏描述，最大长度500
    private String description;

    @Column(name = "introduction", columnDefinition = "TEXT")  // 游戏简介，使用TEXT类型存储较长的内容
    private String introduction;

    @Column(name = "detailed_info", columnDefinition = "TEXT")  // 详细信息，使用TEXT类型存储更详细的内容
    private String detailedInfo;

    @Column(name = "logo_url", length = 255)  // 游戏Logo URL，最大长度255
    private String logoUrl;

    @Column(name = "poster_url", length = 255)  // 游戏宣传图URL，最大长度255
    private String posterUrl;

    @Column(name = "categories", length = 255)  // 游戏分类，多个分类用逗号分隔
    private String categories;

    @Column(name = "create_time")  // 创建时间
    private Date createTime;

    @Column(name = "is_featured")  // 是否推荐（首页大图展示）
    private Boolean isFeatured = false;

    @Column(name = "total_views", columnDefinition = "int default 0")  // 总浏览量（攻略+问答）
    private Integer totalViews = 0;

    // 构造方法
    public Game() {
        this.createTime = new Date();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getDetailedInfo() {
        return detailedInfo;
    }

    public void setDetailedInfo(String detailedInfo) {
        this.detailedInfo = detailedInfo;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Integer getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Integer totalViews) {
        this.totalViews = totalViews;
    }
}