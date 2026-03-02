package com.huadetec.gamecommunity.service;

import com.huadetec.gamecommunity.entity.FileResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 文件资源服务接口
 */
public interface FileResourceService {

    /**
     * 保存文件资源信息
     * @param fileResource 文件资源对象
     * @return 保存后的文件资源
     */
    FileResource save(FileResource fileResource);

    /**
     * 根据ID查找文件资源
     * @param id 文件资源ID
     * @return 文件资源对象
     */
    Optional<FileResource> findById(Long id);

    /**
     * 根据文件名查找文件资源
     * @param fileName 文件名
     * @return 文件资源对象
     */
    Optional<FileResource> findByFileName(String fileName);

    /**
     * 获取所有未删除的文件资源
     * @return 文件资源列表
     */
    List<FileResource> findAll();

    /**
     * 根据上传者ID获取文件资源列表
     * @param uploaderId 上传者ID
     * @return 文件资源列表
     */
    List<FileResource> findByUploaderId(Long uploaderId);

    /**
     * 根据上传者用户名获取文件资源列表
     * @param uploaderName 上传者用户名
     * @return 文件资源列表
     */
    List<FileResource> findByUploaderName(String uploaderName);

    /**
     * 根据文件类型获取文件资源列表
     * @param fileType 文件类型
     * @return 文件资源列表
     */
    List<FileResource> findByFileType(String fileType);

    /**
     * 根据分类获取文件资源列表
     * @param category 分类
     * @return 文件资源列表
     */
    List<FileResource> findByCategory(String category);

    /**
     * 根据多个条件组合查询文件资源
     * @param fileType 文件类型
     * @param category 分类
     * @param uploaderId 上传者ID
     * @return 文件资源列表
     */
    List<FileResource> findByMultipleCriteria(String fileType, String category, Long uploaderId);

    /**
     * 根据原始文件名模糊搜索文件资源
     * @param keyword 关键词
     * @return 文件资源列表
     */
    List<FileResource> searchByOriginalName(String keyword);

    /**
     * 根据标签搜索文件资源
     * @param tag 标签
     * @return 文件资源列表
     */
    List<FileResource> searchByTags(String tag);

    /**
     * 删除文件资源（逻辑删除）
     * @param id 文件资源ID
     * @param deletedBy 删除者ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id, Long deletedBy);

    /**
     * 永久删除文件资源（物理删除）
     * @param id 文件资源ID
     * @return 是否删除成功
     */
    boolean permanentDeleteById(Long id);

    /**
     * 恢复已删除的文件资源
     * @param id 文件资源ID
     * @return 是否恢复成功
     */
    boolean restoreById(Long id);

    /**
     * 增加文件下载次数
     * @param id 文件资源ID
     */
    void incrementDownloadCount(Long id);

    /**
     * 增加文件查看次数
     * @param id 文件资源ID
     */
    void incrementViewCount(Long id);

    /**
     * 更新文件资源信息
     * @param fileResource 文件资源对象
     * @return 更新后的文件资源
     */
    FileResource update(FileResource fileResource);

    /**
     * 检查文件名是否已存在
     * @param fileName 文件名
     * @return 是否存在
     */
    boolean existsByFileName(String fileName);

    /**
     * 统计上传者的文件数量
     * @param uploaderId 上传者ID
     * @return 文件数量
     */
    Long countByUploaderId(Long uploaderId);

    /**
     * 统计文件类型的文件数量
     * @param fileType 文件类型
     * @return 文件数量
     */
    Long countByFileType(String fileType);

    /**
     * 统计分类的文件数量
     * @param category 分类
     * @return 文件数量
     */
    Long countByCategory(String category);

    /**
     * 获取下载次数最多的文件
     * @param limit 限制数量
     * @return 文件资源列表
     */
    List<FileResource> getTopDownloadedFiles(int limit);

    /**
     * 获取查看次数最多的文件
     * @param limit 限制数量
     * @return 文件资源列表
     */
    List<FileResource> getTopViewedFiles(int limit);

    /**
     * 创建文件资源记录（用于文件上传后保存信息）
     * @param originalName 原始文件名
     * @param fileName 存储文件名
     * @param filePath 文件存储路径
     * @param fileUrl 文件访问URL
     * @param fileSize 文件大小
     * @param fileType 文件类型
     * @param uploaderId 上传者ID
     * @param uploaderName 上传者用户名
     * @return 创建的文件资源
     */
    FileResource createFileResource(String originalName, String fileName, String filePath, 
                                   String fileUrl, Long fileSize, String fileType, 
                                   Long uploaderId, String uploaderName);
}