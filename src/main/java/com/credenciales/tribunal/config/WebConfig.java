package com.credenciales.tribunal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${qr.storage.path}")
    private String qrStoragePath;
    
    @Value("${qr.storage.url-prefix}")
    private String qrUrlPrefix;
    
    @Value("${qr.base-url}")
    private String baseUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(qrUrlPrefix + "**")
                .addResourceLocations("file:" + qrStoragePath);
    }
}