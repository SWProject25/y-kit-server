package com.twojz.y_kit.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${UPLOAD_PATH}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String filePath = "file:" + (uploadPath.endsWith("/") ? uploadPath : uploadPath + "/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(filePath);
    }
}