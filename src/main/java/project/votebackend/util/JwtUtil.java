package project.votebackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}") // application.properties 또는 .yml 파일에서 jwt.secret 값을 주입받음
    private String SECRET_KEY;

    public String generateToken(String username, Long userId) {
        // 토큰 유효 시간 설정 (1일 = 86400000 밀리초)
        long EXPIRATION_TIME = 86400000;

        // JWT 토큰 생성
        return Jwts.builder()
                .setSubject(username)               // 사용자 이름을 subject로 설정
                .claim("userId", userId)           // 사용자 ID를 추가 정보(claim)로 설정
                .setIssuedAt(new Date())           // 토큰 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 만료 시간 설정
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY) // HS512 알고리즘과 비밀 키로 서명
                .compact();                        // 최종적으로 JWT 문자열 반환
    }

    public String extractUsername(String token) {
        // JWT에서 subject(=username) 추출
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        // JWT의 만료일이 현재 시간 이후인지 확인하여 유효성 판단
        return !getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        // 토큰에서 Claims(페이로드 정보)를 파싱하여 반환
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)         // 서명 검증을 위한 비밀 키 설정
                .parseClaimsJws(token)             // 토큰을 파싱하고 서명 검증
                .getBody();                        // 페이로드(Claims) 반환
    }

}
