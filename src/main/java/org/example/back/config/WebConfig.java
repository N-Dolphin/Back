package org.example.back.config;

import lombok.RequiredArgsConstructor;

import org.example.back.config.interceptor.JwtInterceptor;
import org.example.back.config.interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final LogInterceptor logInterceptor;
    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
            .order(1)
            .addPathPatterns("/**");

        registry.addInterceptor(jwtInterceptor)
            .order(2)
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
                "/api/v1/chat/rooms",
                "/api/v1/auth/refresh"
            )
            .excludePathPatterns("/**", HttpMethod.OPTIONS.name());  // OPTIONS 요청 제외
    }

    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/api/**")
    //         .allowedOriginPatterns("*")  // "*" 대신 이것을 사용
    //         .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    //         .allowedHeaders("*")
    //         .exposedHeaders("Authorization")  // Authorization 헤더 노출
    //         .allowCredentials(true)
    //         .maxAge(3600);
    // }
}