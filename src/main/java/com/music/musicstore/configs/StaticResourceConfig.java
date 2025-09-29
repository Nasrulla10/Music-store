package com.music.musicstore.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL path /uploads/music/** to the filesystem uploads directory configured in application.properties
        Path uploadDir = Paths.get("./uploads/music").toAbsolutePath().normalize();
        String uploadPath = uploadDir.toUri().toString();

        registry.addResourceHandler("/uploads/music/**")
            .addResourceLocations(uploadPath)
            .setCachePeriod(3600);
    }
}

