package project.votebackend.controller.email;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.votebackend.dto.email.EmailRequest;
import project.votebackend.dto.email.EmailVerifyRequest;
import project.votebackend.service.email.EmailService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    // 인증 코드 발송
    @PostMapping("/send")
    public ResponseEntity<String> sendCode(@RequestBody EmailRequest request) {
        emailService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    }

    // 인증 코드 검증
    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody EmailVerifyRequest request) {
        boolean result = emailService.verifyCode(request.getEmail(), request.getCode());
        return result
                ? ResponseEntity.ok("이메일 인증 성공")
                : ResponseEntity.badRequest().body("인증 실패 또는 만료된 코드");
    }
}
