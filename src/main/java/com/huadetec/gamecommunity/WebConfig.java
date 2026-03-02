package com.huadetec.gamecommunity;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:./avatars/");
        registry.addResourceHandler("/assets/avatars/**")
                .addResourceLocations("file:./frontend/src/assets/avatars/");
        // 添加对前端dist目录中头像文件的映射
        registry.addResourceHandler("/dist/assets/avatars/**")
                .addResourceLocations("file:./frontend/dist/assets/avatars/");
    }
}