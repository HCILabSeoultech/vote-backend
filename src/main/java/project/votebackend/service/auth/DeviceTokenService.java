package project.votebackend.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.auth.DeviceToken;
import project.votebackend.repository.auth.DeviceTokenRepository;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public void registerOrUpdate(Long userId, String token, String os) {
        DeviceToken e = deviceTokenRepository.findByToken(token)
                .orElseGet(() -> new DeviceToken(userId, token, os));
        e.setUserId(userId);
        e.setOs(os);
        e.setActive(true);
        deviceTokenRepository.save(e);
    }

    @Transactional
    public void deactivate(String token) {
        deviceTokenRepository.findByToken(token).ifPresent(e -> e.setActive(false));
    }
}
