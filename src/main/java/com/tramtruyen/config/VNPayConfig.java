package com.tramtruyen.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Đăng ký bean cấu hình VNPay trong Spring context.
 */
@Configuration
@EnableConfigurationProperties(VNPayProperties.class)
public class VNPayConfig {
}
