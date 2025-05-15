package project.votebackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import project.votebackend.util.JwtUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청 헤더에서 Authorization 값을 가져옴
        String authHeader = request.getHeader("Authorization");

        // Authorization 헤더가 존재하고 "Bearer "로 시작할 경우에만 JWT 처리
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // "Bearer " 이후의 실제 JWT 토큰만 추출
            String token = authHeader.substring(7);

            // JWT에서 username(subject)을 추출
            String username = jwtUtil.extractUsername(token);

            // username이 존재하고, 현재 SecurityContext에 인증 정보가 없을 경우에만 처리
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // DB 또는 캐시에서 사용자 정보 로드
                CustumUserDetails userDetails = (CustumUserDetails) userDetailsService.loadUserByUsername(username);

                // 인증 객체 생성 (비밀번호는 null, 권한은 userDetails에서 가져옴)
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 인증 요청에 대한 상세 정보를 설정 (예: IP, 세션 등)
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Spring Security의 SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 다음 필터로 요청과 응답을 전달 (필터 체인 계속 진행)
        filterChain.doFilter(request, response);
    }
}
