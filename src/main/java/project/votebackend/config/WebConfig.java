package project.votebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 설정 적용
                .allowedOrigins("*") // 모든 도메인에서의 요청 허용 (개발 중에는 "*" 사용, 배포 시에는 특정 도메인으로 제한 권장)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // 허용할 HTTP 메서드 지정
                .allowedHeaders("*"); // 모든 요청 헤더 허용
    }
}
