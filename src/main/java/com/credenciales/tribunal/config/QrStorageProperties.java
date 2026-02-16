package com.credenciales.tribunal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "qr.storage")
@Getter
@Setter
public class QrStorageProperties {
    
    private String path = "C:/qr/";
    private String urlPrefix = "/uploads/qr/";
}