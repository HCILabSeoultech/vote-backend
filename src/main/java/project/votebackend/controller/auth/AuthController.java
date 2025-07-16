package project.votebackend.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import project.votebackend.domain.user.User;
import project.votebackend.dto.login.LoginRequest;
import project.votebackend.dto.login.LoginResponse;
import project.votebackend.dto.signup.UserSignupDto;
import project.votebackend.dto.user.UserUpdateDto;
import project.votebackend.exception.AuthException;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.auth.AuthService;
import project.votebackend.type.ErrorCode;
import project.votebackend.util.JwtUtil;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;


    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid UserSignupDto userSignupDto,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // 모든 에러 메시지 반환
            String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(errorMsg);
        }

        authService.registerUser(userSignupDto);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "회원가입이 완료되었습니다."
        ));
    }

    // 아이디 중복 확인
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameDuplicate(@RequestParam String username) {
        boolean available = authService.isUsernameAvailable(username);
        return ResponseEntity.ok(Map.of("available", available));
    }

    // 전화번호 중복 확인
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Boolean>> checkPhoneDuplicate(@RequestParam String phone) {
        boolean available = authService.isPhoneAvailable(phone);
        return ResponseEntity.ok(Map.of("available", available));
    }

    //닉네임 중복 확인
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Boolean>> checkNameDuplicate(@RequestParam String name) {
        boolean available = authService.isNameAvailable(name);
        return ResponseEntity.ok(Map.of("available", available));
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    //리프레쉬토큰
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = jwtUtil.extractRefreshTokenFromCookie(request);

        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // Redis에서 Refresh Token 검증
        String stored = redisTemplate.opsForValue().get("RT:" + user.getUserId());
        if (stored == null || !stored.equals(refreshToken)) {
            return ResponseEntity.status(401).body("Refresh token mismatch");
        }

        String newAccessToken = jwtUtil.generateToken(username, user.getUserId());
        return ResponseEntity.ok(new LoginResponse("success", newAccessToken));
    }

    //토큰 검증
    @GetMapping("/check")
    public ResponseEntity<?> checkToken(@AuthenticationPrincipal CustumUserDetails user) {
        return ResponseEntity.ok(Map.of(
                "message", "token is valid",
                "username", user.getUsername(),
                "userId", user.getId()
        ));
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustumUserDetails user, HttpServletResponse response) {
        // Redis 삭제
        redisTemplate.delete("RT:" + user.getId());

        // 쿠키도 삭제
        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", expiredCookie.toString());

        return ResponseEntity.ok().build();
    }
}

