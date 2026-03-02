package com.huadetec.gamecommunity.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity  // JPA 注解：表示这是数据库实体
@Table(name = "file_resource")  // 关联数据库表名
public class FileResource {

    @Id  // 主键
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "file_resource_seq_gen")  // 使用表生成策略
    @TableGenerator(
        name = "file_resource_seq_gen",
        table = "sequence_table",
        pkColumnName = "seq_name",
        valueColumnName = "seq_count",
        pkColumnValue = "file_resource_seq",
        allocationSize = 1
    )
    private Long id;

    @Column(name = "original_name", nullable = false, length = 500)  // 原始文件名
    private String originalName;

    @Column(name = "file_name", nullable = false, length = 255)  // 存储文件名（UUID格式）
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 1000)  // 文件存储路径
    private String filePath;

    @Column(name = "file_url", nullable = false, length = 1000)  // 文件访问URL
    private String fileUrl;

    @Column(name = "file_size", nullable = false)  // 文件大小（字节）
    private Long fileSize;

    @Column(name = "file_type", length = 100)  // 文件类型/扩展名
    private String fileType;

    @Column(name = "uploader_id", nullable = false)  // 上传者用户ID
    private Long uploaderId;

    @Column(name = "uploader_name", nullable = false, length = 255)  // 上传者用户名
    private String uploaderName;

    @CreationTimestamp  // 自动填充上传时间
    @Column(name = "upload_time", nullable = false, updatable = false)
    private Date uploadTime;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "tinyint(1) default 0")  // 是否已删除
    private Boolean isDeleted = false;

    @Column(name = "delete_time")  // 删除时间
    private Date deleteTime;

    @Column(name = "deleted_by")  // 删除者用户ID
    private Long deletedBy;

    @Column(name = "description", columnDefinition = "text")  // 文件描述
    private String description;

    @Column(name = "category", length = 100)  // 文件分类
    private String category;

    @Column(name = "tags", length = 500)  // 文件标签
    private String tags;

    @Column(name = "download_count", nullable = false, columnDefinition = "int default 0")  // 下载次数
    private Integer downloadCount = 0;

    @Column(name = "view_count", nullable = false, columnDefinition = "int default 0")  // 查看次数
    private Integer viewCount = 0;

    // 默认构造函数
    public FileResource() {
    }

    // 带参数的构造函数
    public FileResource(String originalName, String fileName, String filePath, String fileUrl, 
                       Long fileSize, String fileType, Long uploaderId, String uploaderName) {
        this.originalName = originalName;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.uploaderId = uploaderId;
        this.uploaderName = uploaderName;
    }

    // Getter 和 Setter 方法

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public Long getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(Long deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    @Override
    public String toString() {
        return "FileResource{" +
                "id=" + id +
                ", originalName='" + originalName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", uploaderId=" + uploaderId +
                ", uploaderName='" + uploaderName + '\'' +
                ", uploadTime=" + uploadTime +
                ", isDeleted=" + isDeleted +
                ", deleteTime=" + deleteTime +
                ", deletedBy=" + deletedBy +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", tags='" + tags + '\'' +
                ", downloadCount=" + downloadCount +
                ", viewCount=" + viewCount +
                '}';
    }
}