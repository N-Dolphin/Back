package org.example.back.config;

import lombok.RequiredArgsConstructor;

import org.example.back.chat.config.CorsProperties;
import org.example.back.config.interceptor.JwtInterceptor;
import org.example.back.config.interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final LogInterceptor logInterceptor;
    private final JwtInterceptor jwtInterceptor;
    private final CorsProperties corsProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
            .order(1)
            .addPathPatterns("/**");

        registry.addInterceptor(jwtInterceptor)
            .order(2)
            .addPathPatterns("/api/v1/auth/**")
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns(
                "/api/v1/producer/send",
                "/hc", "/env",
                "/api/v1/test-redis",
                "/api/v1/auth",
                "/api/v1/auth/home",
                "/api/v1/auth/sign-in",
                "/api/v1/auth/sign-up",
                "/api/v1/auth/email-certification",
                "/api/v1/auth/check-certification",
                "/api/v1/auth/kakao",
                // WebSocket 관련 경로 추가
                "/api/v1/test/**",
                "/api/v1/ws-chat/**",
                "/topic/**",
                "/queue/**",
                "/app/**"
            );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(corsProperties.getAllowedOrigins().toArray(String[]::new))
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}