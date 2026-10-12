package com.tramtruyen.security;

import com.tramtruyen.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final UserRepository userRepository;
    private final AccountLockService accountLockService;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                          CustomAuthenticationFailureHandler customAuthenticationFailureHandler,
                          UserRepository userRepository,
                          AccountLockService accountLockService,
                          CustomUserDetailsService customUserDetailsService) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
        this.userRepository = userRepository;
        this.accountLockService = accountLockService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
        ).authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/verify-otp", "/login", "/account-locked", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/admin/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                .requestMatchers("/user/**", "/tu-sach", "/wallet", "/wallet/**").authenticated()
                .anyRequest().permitAll()
        ).formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler(customAuthenticationFailureHandler)
                .permitAll()
        ).oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                )
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler(customAuthenticationFailureHandler)
        ).rememberMe(remember -> remember
                .key("tramtruyen-remember-me-secret-key-swp391-2025")
                .tokenValiditySeconds(30 * 24 * 60 * 60)
                .userDetailsService(customUserDetailsService)
                .rememberMeParameter("remember-me")
                .rememberMeCookieName("remember-me")
        ).logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
        );
        UserStatusCheckFilter userStatusCheckFilter = new UserStatusCheckFilter(userRepository, accountLockService);
        http.addFilterBefore(userStatusCheckFilter, AuthorizationFilter.class);
        return http.build();
    }
}
