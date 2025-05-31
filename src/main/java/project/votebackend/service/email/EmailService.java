package project.votebackend.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import project.votebackend.domain.auth.VerificationCode;
import project.votebackend.exception.AuthException;
import project.votebackend.repository.auth.VerificationCodeRepository;
import project.votebackend.type.ErrorCode;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final VerificationCodeRepository verificationCodeRepository;

    // 인증 코드 전송
    public void sendVerificationCode(String email) {
        String code = createRandomCode();

        // DB에 저장 (이메일, 코드, 생성 시간)
        VerificationCode entity = VerificationCode.builder()
                .email(email)
                .code(code)
                .build();
        verificationCodeRepository.save(entity);

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증 코드");
        message.setText("인증 코드는: " + code);
        mailSender.send(message);
    }

    private String createRandomCode() {
        return String.valueOf((int)((Math.random() * 900000) + 100000)); // 6자리 숫자
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code) {
        VerificationCode vc = verificationCodeRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new AuthException(ErrorCode.CODE_NOT_MATCHED));

        // 유효 시간: 5분
        if (Duration.between(vc.getCreatedAt(), LocalDateTime.now()).toMinutes() > 5) {
            return false;
        }

        return vc.getCode().equals(code);
    }
}
