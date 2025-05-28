package project.votebackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import project.votebackend.security.JwtAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CORS 설정을 기본값으로 활성화
                .cors(Customizer.withDefaults())

                // CSRF 보호 비활성화 (JWT 방식이므로 필요 없음)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션을 사용하지 않음 - JWT 방식 사용을 위해 STATELESS 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청에 대한 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입, 공유 등의 인증 없이 접근 가능한 엔드포인트
                        .requestMatchers("/location/verify","/auth/**", "/image/upload", "/share/vote/**").permitAll()

                        // 아래 엔드포인트는 인증된 사용자만 접근 가능
                        .requestMatchers(
                                "/vote/**",         // 투표 관련
                                "/reaction/**",     // 좋아요/북마크 등 반응
                                "/storage/**",      // 저장소 관련
                                "/user/**",          // 유저 관련
                                "/comment/**",       // 댓글
                                "/comment-like/**",  // 댓글 좋아요
                                "/search/**",        // 검색
                                "/follow/**",         // 팔로우
                                "/rank/**"            // 랭킹
                        ).authenticated()

                        // 그 외 요청은 모두 허용
                        .anyRequest().permitAll()
                )

                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 최종적으로 SecurityFilterChain 객체를 빌드하여 반환
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호 암호화를 위한 BCrypt 인코더 빈 등록
        return new BCryptPasswordEncoder();
    }
}
