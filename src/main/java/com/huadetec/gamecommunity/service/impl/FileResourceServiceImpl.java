package com.huadetec.gamecommunity.service.impl;

import com.huadetec.gamecommunity.entity.FileResource;
import com.huadetec.gamecommunity.repository.FileResourceRepository;
import com.huadetec.gamecommunity.service.FileResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 文件资源服务实现类
 * 实现文件资源相关的业务逻辑
 */
@Service
public class FileResourceServiceImpl implements FileResourceService {

    private static final Logger logger = LoggerFactory.getLogger(FileResourceServiceImpl.class);

    @Autowired
    private FileResourceRepository fileResourceRepository;

    @Override
    public FileResource save(FileResource fileResource) {
        logger.info("保存文件资源信息");
        // 参数校验
        Assert.notNull(fileResource, "文件资源对象不能为空");
        Assert.hasText(fileResource.getOriginalName(), "原始文件名不能为空");
        Assert.hasText(fileResource.getFileName(), "存储文件名不能为空");
        Assert.hasText(fileResource.getFilePath(), "文件存储路径不能为空");
        Assert.hasText(fileResource.getFileUrl(), "文件访问URL不能为空");
        Assert.notNull(fileResource.getFileSize(), "文件大小不能为空");
        Assert.notNull(fileResource.getUploaderId(), "上传者ID不能为空");
        Assert.hasText(fileResource.getUploaderName(), "上传者用户名不能为空");
        
        // 设置默认值
        if (fileResource.getIsDeleted() == null) {
            fileResource.setIsDeleted(false);
        }
        if (fileResource.getDownloadCount() == null) {
            fileResource.setDownloadCount(0);
        }
        if (fileResource.getViewCount() == null) {
            fileResource.setViewCount(0);
        }
        
        // 保存文件资源
        FileResource savedResource = fileResourceRepository.save(fileResource);
        logger.info("文件资源保存成功，ID: {}, 文件名: {}", savedResource.getId(), savedResource.getFileName());
        
        return savedResource;
    }

    @Override
    public Optional<FileResource> findById(Long id) {
        logger.info("根据ID查找文件资源: {}", id);
        return fileResourceRepository.findById(id);
    }

    @Override
    public Optional<FileResource> findByFileName(String fileName) {
        logger.info("根据文件名查找文件资源: {}", fileName);
        return fileResourceRepository.findByFileName(fileName);
    }

    @Override
    public List<FileResource> findAll() {
        logger.info("获取所有未删除的文件资源");
        return fileResourceRepository.findByIsDeletedFalse();
    }

    @Override
    public List<FileResource> findByUploaderId(Long uploaderId) {
        logger.info("根据上传者ID获取文件资源列表: {}", uploaderId);
        return fileResourceRepository.findByUploaderIdAndIsDeletedFalse(uploaderId);
    }

    @Override
    public List<FileResource> findByUploaderName(String uploaderName) {
        logger.info("根据上传者用户名获取文件资源列表: {}", uploaderName);
        return fileResourceRepository.findByUploaderName(uploaderName);
    }

    @Override
    public List<FileResource> findByFileType(String fileType) {
        logger.info("根据文件类型获取文件资源列表: {}", fileType);
        return fileResourceRepository.findByFileTypeAndIsDeletedFalse(fileType);
    }

    @Override
    public List<FileResource> findByCategory(String category) {
        logger.info("根据分类获取文件资源列表: {}", category);
        return fileResourceRepository.findByCategoryAndIsDeletedFalse(category);
    }

    @Override
    public List<FileResource> findByMultipleCriteria(String fileType, String category, Long uploaderId) {
        logger.info("根据多个条件组合查询文件资源，文件类型: {}, 分类: {}, 上传者ID: {}", fileType, category, uploaderId);
        return fileResourceRepository.findByMultipleCriteria(fileType, category, uploaderId);
    }

    @Override
    public List<FileResource> searchByOriginalName(String keyword) {
        logger.info("根据原始文件名模糊搜索文件资源: {}", keyword);
        return fileResourceRepository.findByOriginalNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<FileResource> searchByTags(String tag) {
        logger.info("根据标签搜索文件资源: {}", tag);
        return fileResourceRepository.findByTagsContainingIgnoreCase(tag);
    }

    @Override
    @Transactional
    public boolean deleteById(Long id, Long deletedBy) {
        logger.info("删除文件资源（逻辑删除），ID: {}, 删除者ID: {}", id, deletedBy);
        
        // 验证文件资源是否存在
        Optional<FileResource> fileResourceOptional = fileResourceRepository.findById(id);
        if (!fileResourceOptional.isPresent()) {
            logger.warn("文件资源不存在，ID: {}", id);
            return false;
        }
        
        FileResource fileResource = fileResourceOptional.get();
        
        // 如果已经删除，直接返回成功
        if (fileResource.getIsDeleted()) {
            logger.info("文件资源已被删除，ID: {}", id);
            return true;
        }
        
        // 执行逻辑删除
        fileResource.setIsDeleted(true);
        fileResource.setDeleteTime(new Date());
        fileResource.setDeletedBy(deletedBy);
        
        fileResourceRepository.save(fileResource);
        logger.info("文件资源逻辑删除成功，ID: {}", id);
        
        return true;
    }

    @Override
    @Transactional
    public boolean permanentDeleteById(Long id) {
        logger.info("永久删除文件资源（物理删除），ID: {}", id);
        
        // 验证文件资源是否存在
        if (!fileResourceRepository.existsById(id)) {
            logger.warn("文件资源不存在，ID: {}", id);
            return false;
        }
        
        // 执行物理删除
        fileResourceRepository.deleteById(id);
        logger.info("文件资源物理删除成功，ID: {}", id);
        
        return true;
    }

    @Override
    @Transactional
    public boolean restoreById(Long id) {
        logger.info("恢复已删除的文件资源，ID: {}", id);
        
        // 验证文件资源是否存在
        Optional<FileResource> fileResourceOptional = fileResourceRepository.findById(id);
        if (!fileResourceOptional.isPresent()) {
            logger.warn("文件资源不存在，ID: {}", id);
            return false;
        }
        
        FileResource fileResource = fileResourceOptional.get();
        
        // 如果未删除，直接返回成功
        if (!fileResource.getIsDeleted()) {
            logger.info("文件资源未被删除，ID: {}", id);
            return true;
        }
        
        // 执行恢复操作
        fileResource.setIsDeleted(false);
        fileResource.setDeleteTime(null);
        fileResource.setDeletedBy(null);
        
        fileResourceRepository.save(fileResource);
        logger.info("文件资源恢复成功，ID: {}", id);
        
        return true;
    }

    @Override
    @Transactional
    public void incrementDownloadCount(Long id) {
        logger.info("增加文件下载次数，ID: {}", id);
        
        Optional<FileResource> fileResourceOptional = fileResourceRepository.findById(id);
        if (fileResourceOptional.isPresent()) {
            FileResource fileResource = fileResourceOptional.get();
            fileResource.setDownloadCount(fileResource.getDownloadCount() + 1);
            fileResourceRepository.save(fileResource);
            logger.info("文件下载次数增加成功，ID: {}, 当前下载次数: {}", id, fileResource.getDownloadCount());
        } else {
            logger.warn("文件资源不存在，无法增加下载次数，ID: {}", id);
        }
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        logger.info("增加文件查看次数，ID: {}", id);
        
        Optional<FileResource> fileResourceOptional = fileResourceRepository.findById(id);
        if (fileResourceOptional.isPresent()) {
            FileResource fileResource = fileResourceOptional.get();
            fileResource.setViewCount(fileResource.getViewCount() + 1);
            fileResourceRepository.save(fileResource);
            logger.info("文件查看次数增加成功，ID: {}, 当前查看次数: {}", id, fileResource.getViewCount());
        } else {
            logger.warn("文件资源不存在，无法增加查看次数，ID: {}", id);
        }
    }

    @Override
    public FileResource update(FileResource fileResource) {
        logger.info("更新文件资源信息，ID: {}", fileResource.getId());
        
        // 参数校验
        Assert.notNull(fileResource.getId(), "文件资源ID不能为空");
        Assert.hasText(fileResource.getOriginalName(), "原始文件名不能为空");
        Assert.hasText(fileResource.getFileName(), "存储文件名不能为空");
        
        // 验证文件资源是否存在
        if (!fileResourceRepository.existsById(fileResource.getId())) {
            throw new RuntimeException("文件资源不存在：" + fileResource.getId());
        }
        
        // 保存更新后的文件资源
        FileResource updatedResource = fileResourceRepository.save(fileResource);
        logger.info("文件资源更新成功，ID: {}", fileResource.getId());
        
        return updatedResource;
    }

    @Override
    public boolean existsByFileName(String fileName) {
        return fileResourceRepository.existsByFileName(fileName);
    }

    @Override
    public Long countByUploaderId(Long uploaderId) {
        logger.info("统计上传者的文件数量，上传者ID: {}", uploaderId);
        return fileResourceRepository.countByUploaderId(uploaderId);
    }

    @Override
    public Long countByFileType(String fileType) {
        logger.info("统计文件类型的文件数量，文件类型: {}", fileType);
        return fileResourceRepository.countByFileType(fileType);
    }

    @Override
    public Long countByCategory(String category) {
        logger.info("统计分类的文件数量，分类: {}", category);
        return fileResourceRepository.countByCategory(category);
    }

    @Override
    public List<FileResource> getTopDownloadedFiles(int limit) {
        logger.info("获取下载次数最多的文件，限制数量: {}", limit);
        return fileResourceRepository.findTopByDownloadCount(limit);
    }

    @Override
    public List<FileResource> getTopViewedFiles(int limit) {
        logger.info("获取查看次数最多的文件，限制数量: {}", limit);
        return fileResourceRepository.findTopByViewCount(limit);
    }

    @Override
    public FileResource createFileResource(String originalName, String fileName, String filePath, 
                                          String fileUrl, Long fileSize, String fileType, 
                                          Long uploaderId, String uploaderName) {
        logger.info("创建文件资源记录，原始文件名: {}, 存储文件名: {}", originalName, fileName);
        
        // 创建新的文件资源对象
        FileResource fileResource = new FileResource(originalName, fileName, filePath, fileUrl, 
                                                   fileSize, fileType, uploaderId, uploaderName);
        
        // 保存文件资源
        return save(fileResource);
    }
}