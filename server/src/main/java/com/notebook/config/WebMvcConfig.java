package com.notebook.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置类
 * 配置跨域、静态资源等
 *
 * @author notebook
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 应用启动时创建上传目录
     */
    @PostConstruct
    public void init() {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                logger.info("创建上传目录成功: {}", uploadDir.toAbsolutePath());
            }
            
            Path avatarDir = uploadDir.resolve("avatar");
            if (!Files.exists(avatarDir)) {
                Files.createDirectories(avatarDir);
                logger.info("创建头像目录成功: {}", avatarDir.toAbsolutePath());
            }
            
            // 设置目录权限（Linux/Mac）
            try {
                // 设置上传目录权限为 755
                Runtime.getRuntime().exec("chmod 755 " + uploadDir.toAbsolutePath());
                Runtime.getRuntime().exec("chmod 755 " + avatarDir.toAbsolutePath());
                logger.info("设置目录权限成功");
            } catch (Exception e) {
                // Windows 系统可能没有 chmod 命令，忽略此错误
                logger.debug("设置目录权限失败（可能是Windows系统）: {}", e.getMessage());
            }
            
        } catch (IOException e) {
            logger.error("创建上传目录失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法创建上传目录: " + uploadPath, e);
        }
    }

    /**
     * 配置跨域请求
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源映射
     * 映射上传文件目录
     *
     * @param registry 资源注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
