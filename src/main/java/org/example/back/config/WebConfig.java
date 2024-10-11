package org.example.back.config;

import lombok.RequiredArgsConstructor;
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


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .order(1) //첫번째로 실행 될 인터셉터로 등록
                .addPathPatterns("/**"); //  "/"하위에 전부 적용

        registry.addInterceptor(jwtInterceptor)
                .order(2)
                .addPathPatterns("/api/v1/auth/**") // 인증이 필요한 경로에 대해 인터셉터 적용
                .addPathPatterns("/api/v1/**") // 인증이 필요한 경로에 대해 인터셉터 적용
                .excludePathPatterns(
                        "/hc", "/env",
                        "/api/v1/test-redis",
                        "/api/v1/auth",
                        "/api/v1/auth/home",
                        "/api/v1/auth/sign-in",
                        "/api/v1/auth/sign-up",
                        "/api/v1/auth/email-certification",
                        "/api/v1/auth/check-certification",
                        "/api/v1/auth/kakao"
                ); // 인증이 필요하지 않은 경로는 제외

   }

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .maxAge(3600);
    }
}