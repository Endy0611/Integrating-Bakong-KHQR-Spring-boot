package com.example.integration_with_bakong_khqr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bakong.api")
public class BakongProperties {
    private String token;
    private String baseUrl;
    private String accountId;
    private String merchantName;
    private String merchantCity;
}