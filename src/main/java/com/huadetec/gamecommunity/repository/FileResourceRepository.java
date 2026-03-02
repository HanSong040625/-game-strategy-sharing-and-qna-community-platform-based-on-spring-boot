package com.huadetec.gamecommunity.repository;

import com.huadetec.gamecommunity.entity.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository  // 标记为数据访问层组件
// JpaRepository<实体类, 主键类型>：自带 CRUD 方法（save、findById 等）
public interface FileResourceRepository extends JpaRepository<FileResource, Long> {

    // 根据文件名查找文件
    Optional<FileResource> findByFileName(String fileName);

    // 根据上传者ID查找文件列表
    List<FileResource> findByUploaderId(Long uploaderId);

    // 根据上传者用户名查找文件列表
    List<FileResource> findByUploaderName(String uploaderName);

    // 根据文件类型查找文件列表
    List<FileResource> findByFileType(String fileType);

    // 根据分类查找文件列表
    List<FileResource> findByCategory(String category);

    // 查找未删除的文件列表
    List<FileResource> findByIsDeletedFalse();

    // 查找已删除的文件列表
    List<FileResource> findByIsDeletedTrue();

    // 根据上传者ID查找未删除的文件列表
    List<FileResource> findByUploaderIdAndIsDeletedFalse(Long uploaderId);

    // 根据文件类型查找未删除的文件列表
    List<FileResource> findByFileTypeAndIsDeletedFalse(String fileType);

    // 根据分类查找未删除的文件列表
    List<FileResource> findByCategoryAndIsDeletedFalse(String category);

    // 根据文件名前缀查找文件（用于模糊搜索）
    List<FileResource> findByFileNameStartingWith(String prefix);

    // 根据原始文件名查找文件（模糊搜索）
    List<FileResource> findByOriginalNameContainingIgnoreCase(String keyword);

    // 根据标签查找文件（模糊搜索）
    List<FileResource> findByTagsContainingIgnoreCase(String tag);

    // 根据文件大小范围查找文件
    List<FileResource> findByFileSizeBetween(Long minSize, Long maxSize);

    // 根据上传时间范围查找文件
    List<FileResource> findByUploadTimeBetween(java.util.Date startDate, java.util.Date endDate);

    // 统计上传者的文件数量
    @Query("SELECT COUNT(f) FROM FileResource f WHERE f.uploaderId = :uploaderId AND f.isDeleted = false")
    Long countByUploaderId(@Param("uploaderId") Long uploaderId);

    // 统计文件类型的文件数量
    @Query("SELECT COUNT(f) FROM FileResource f WHERE f.fileType = :fileType AND f.isDeleted = false")
    Long countByFileType(@Param("fileType") String fileType);

    // 统计分类的文件数量
    @Query("SELECT COUNT(f) FROM FileResource f WHERE f.category = :category AND f.isDeleted = false")
    Long countByCategory(@Param("category") String category);

    // 获取下载次数最多的文件
    @Query("SELECT f FROM FileResource f WHERE f.isDeleted = false ORDER BY f.downloadCount DESC")
    List<FileResource> findTopByDownloadCount(@Param("limit") int limit);

    // 获取查看次数最多的文件
    @Query("SELECT f FROM FileResource f WHERE f.isDeleted = false ORDER BY f.viewCount DESC")
    List<FileResource> findTopByViewCount(@Param("limit") int limit);

    // 根据多个条件组合查询
    @Query("SELECT f FROM FileResource f WHERE " +
           "(:fileType IS NULL OR f.fileType = :fileType) AND " +
           "(:category IS NULL OR f.category = :category) AND " +
           "(:uploaderId IS NULL OR f.uploaderId = :uploaderId) AND " +
           "f.isDeleted = false")
    List<FileResource> findByMultipleCriteria(@Param("fileType") String fileType,
                                              @Param("category") String category,
                                              @Param("uploaderId") Long uploaderId);

    // 检查文件名是否已存在（用于上传时验证）
    boolean existsByFileName(String fileName);

    // 检查原始文件名是否已存在（用于上传时验证）
    boolean existsByOriginalNameAndUploaderId(String originalName, Long uploaderId);
}