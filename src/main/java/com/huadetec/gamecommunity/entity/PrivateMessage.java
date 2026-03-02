package com.huadetec.gamecommunity.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "private_message")
public class PrivateMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "private_message_seq_gen")
    @TableGenerator(
        name = "private_message_seq_gen",
        table = "sequence_table",
        pkColumnName = "seq_name",
        valueColumnName = "seq_count",
        pkColumnValue = "private_message_seq",
        allocationSize = 1
    )
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "message_type", nullable = false, length = 50)
    private String messageType = "TEXT";

    @CreationTimestamp
    @Column(name = "send_time", nullable = false, updatable = false)
    private Date sendTime;

    // 构造函数
    public PrivateMessage() {}

    public PrivateMessage(Long senderId, Long receiverId, String content, String messageType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.messageType = messageType;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public String toString() {
        return "PrivateMessage{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", content='" + content + '\'' +
                ", messageType='" + messageType + '\'' +
                ", sendTime=" + sendTime +
                '}';
    }
}