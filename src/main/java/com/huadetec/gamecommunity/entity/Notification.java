package com.huadetec.gamecommunity.entity;
import org.hibernate.annotations.CreationTimestamp;//自动往容器里设置时间
import javax.persistence.*;//jpa注解,引入jpa标准
import java.util.Date;//创建时间容器

@Entity
@Table(name = "notification")//将java类映射到表notification中
public class Notification{
    @Id//声明主键，将下面的字段映射到数据库中的主键列上
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增策略
    private Long id;//主键id，存放主键的值


//在你的 Notification（通知）和 User（用户）实体之间建立了强大的连接。多条通知（Many）可以属于同一个接收用户（One）。
//这条关系在数据库中是通过 notification 表中的一个名为 recipient_id 的外键列来维护的。
//一个 User（接收人）可以接收多条 Notification（通知）。
//一条 Notification（通知）只能被发送给一个 User（接收人）。
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;//接收人

    @Column(columnDefinition = "TEXT")//将下面的字段在数据库中定义为TEXT
    private String content;//通知内容
    private String type;//通知类型  例如：系统通知、评论通知、点赞通知等 如"answer"表示回答通知
    private Long relatedId;//关联的实体id，例如回答id、评论id等 当用户点击通知时，可以使用此ID跳转到相关的回答、评论或其他内容
    private Boolean isRead = false;//是否已读

    @CreationTimestamp //当对象第一次被保存到数据库时，Hibernate 会自动将当前日期时间设置到这个字段
    @Column(name = "create_time",updatable = false)//指定数据库中的列名为 create_time updatable = false表示这个字段永不修改
    private Date createTime;//创建时间 使用 Date 类型存储时间戳

    // 添加sender字段定义
    // 修改：移除@ManyToOne关联，改为普通字段存储发送者ID
    @Column(name = "sender_id")
    private Long senderId;//消息发送人ID（可以是普通用户ID或管理员用户ID）
    
    // 添加发送者类型字段，用于区分普通用户和管理员用户
    @Column(name = "sender_type")
    private String senderType;//发送者类型："user"或"admin"

    // 添加所有字段的getter和setter方法
    public Long getId() { return id;}
    public void setId(Long id) {this.id = id ;}//this当前对象： new Notification();

    public User getRecipient() { return recipient ;}
    public void setRecipient(User recipient) {this.recipient = recipient ;}

    public Long getSenderId() {return senderId;}
    public void setSenderId(Long senderId) {this.senderId = senderId;}
    
    public String getSenderType() {return senderType;}
    public void setSenderType(String senderType) {this.senderType = senderType;}

    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}

    public String getType() {return type;}
    public void setType(String type) {this.type = type;}

    public Long getRelatedId() {return relatedId;}
    public void setRelatedId(Long relatedId) {this.relatedId = relatedId;}

    public Boolean getIsRead() {return isRead;}
    public void setIsRead(Boolean isRead) {this.isRead = isRead;}


    public Date getCreateTime() {return createTime;}
    public void setCreateTime(Date createTime) {this.createTime = createTime;}


}