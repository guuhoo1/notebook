package com.notebook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 配置跨域、静态资源等
 *
 * @author notebook
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

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
                .addResourceLocations("file:d:/code/notebook/uploads/");
    }
}
