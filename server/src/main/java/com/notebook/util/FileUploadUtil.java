package com.notebook.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件上传工具类
 * 统一处理文件上传逻辑
 */
@Component
public class FileUploadUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadUtil.class);

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.base-url}")
    private String baseUrl;

    /**
     * 应用启动时确保上传目录存在
     */
    @PostConstruct
    public void init() {
        try {
            Path rootDir = Paths.get(uploadPath);
            ensureDirectoryExists(rootDir);
            
            // 创建常用子目录
            ensureDirectoryExists(rootDir.resolve("avatar"));
            ensureDirectoryExists(rootDir.resolve("note"));
            ensureDirectoryExists(rootDir.resolve("temp"));
            
            logger.info("文件上传目录初始化完成: {}", uploadPath);
        } catch (IOException e) {
            logger.error("文件上传目录初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法初始化上传目录: " + uploadPath, e);
        }
    }

    /**
     * 确保目录存在，如果不存在则创建
     */
    private void ensureDirectoryExists(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            logger.info("创建目录: {}", dir.toAbsolutePath());
            
            // 尝试设置权限（Linux/Mac）
            try {
                Runtime.getRuntime().exec("chmod 755 " + dir.toAbsolutePath());
            } catch (Exception e) {
                logger.debug("设置目录权限失败（可能是Windows系统）: {}", e.getMessage());
            }
        }
    }

    /**
     * 上传头像文件
     *
     * @param file    头像文件
     * @param userId  用户ID
     * @return 头像URL
     * @throws IOException 上传失败时抛出
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的头像文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("只支持JPG和PNG格式的图片");
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("图片大小不能超过2MB");
        }

        // 构建文件路径
        Path avatarDir = Paths.get(uploadPath, "avatar");
        ensureDirectoryExists(avatarDir);

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String fileName = userId + "_" + System.currentTimeMillis() + extension;
        Path filePath = avatarDir.resolve(fileName);

        // 写入文件
        Files.write(filePath, file.getBytes());
        logger.info("头像上传成功: {}", filePath);

        // 返回URL
        return baseUrl + "/avatar/" + fileName;
    }

    /**
     * 上传笔记附件
     *
     * @param file   附件文件
     * @param noteId 笔记ID
     * @return 附件URL
     * @throws IOException 上传失败时抛出
     */
    public String uploadNoteAttachment(MultipartFile file, Long noteId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }

        // 限制文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }

        Path noteDir = Paths.get(uploadPath, "note");
        ensureDirectoryExists(noteDir);

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String fileName = noteId + "_" + System.currentTimeMillis() + extension;
        Path filePath = noteDir.resolve(fileName);

        Files.write(filePath, file.getBytes());
        logger.info("笔记附件上传成功: {}", filePath);

        return baseUrl + "/note/" + fileName;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径（相对于上传目录）
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        try {
            Path fullPath = Paths.get(uploadPath, filePath);
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                logger.info("文件删除成功: {}", fullPath);
                return true;
            }
            return false;
        } catch (IOException e) {
            logger.error("文件删除失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取上传根路径
     */
    public String getUploadPath() {
        return uploadPath;
    }

    /**
     * 获取基础URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
