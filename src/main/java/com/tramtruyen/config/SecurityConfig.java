package com.tramtruyen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập công khai vào Homepage và các tài nguyên tĩnh
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/novel/**", "/category/**").permitAll()
                // Mọi request khác tạm thời cũng cho phép để dễ code, sau này sẽ cấu hình sau
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Tạm tắt CSRF để dễ test
        
        return http.build();
    }
}
