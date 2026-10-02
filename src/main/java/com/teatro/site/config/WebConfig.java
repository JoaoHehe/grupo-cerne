package com.teatro.site.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    // Tudo que for pedido em /uploads/... é buscado na pasta de imagens
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String local = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        if (!local.endsWith("/")) {
            local += "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(local);
    }
}