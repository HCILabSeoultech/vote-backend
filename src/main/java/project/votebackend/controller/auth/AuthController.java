package project.votebackend.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import project.votebackend.domain.user.User;
import project.votebackend.dto.login.LoginRequest;
import project.votebackend.dto.signup.UserSignupDto;
import project.votebackend.dto.user.UserUpdateDto;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.auth.AuthService;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

        User newUser = authService.registerUser(userSignupDto);
        return ResponseEntity.ok(newUser);
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

    //로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}

