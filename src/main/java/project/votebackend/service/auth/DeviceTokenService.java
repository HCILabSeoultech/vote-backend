package project.votebackend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.auth.DeviceToken;
import project.votebackend.repository.auth.DeviceTokenRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public void registerOrUpdate(Long userId, String token, String os) {
        // Expo 푸시 토큰 유효성 검증
        if (!isValidExpoPushToken(token)) {
            log.warn("유효하지 않은 Expo 푸시 토큰: {}", token.substring(0, Math.min(20, token.length())) + "...");
            return; // 유효하지 않은 토큰은 저장하지 않음
        }

        DeviceToken e = deviceTokenRepository.findByToken(token)
                .orElseGet(() -> new DeviceToken(userId, token, os));
        e.setUserId(userId);
        e.setOs(os);
        e.setActive(true);
        deviceTokenRepository.save(e);

        log.info("Expo 푸시 토큰 등록/갱신 완료: userId={}, token={}...", userId, token.substring(0, Math.min(20, token.length())));
    }

    @Transactional
    public void deactivate(String token) {
        deviceTokenRepository.findByToken(token).ifPresent(e -> e.setActive(false));
    }

    private boolean isValidExpoPushToken(String token) {
        return token != null &&
                token.startsWith("ExponentPushToken[") &&
                token.endsWith("]");
    }
}
