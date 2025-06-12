package emo.chen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class photo implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 映射到 D:/uploads/goods/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/uploads/goods/");
    }
}
