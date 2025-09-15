package project.votebackend.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.login.DeactivateRequest;
import project.votebackend.dto.login.RegisterRequest;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.auth.DeviceTokenService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    // 로그인 직후 디바이스 토큰 등록/갱신
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @AuthenticationPrincipal CustumUserDetails user,
            @RequestBody RegisterRequest request) {
        deviceTokenService.registerOrUpdate(user.getId(), request.getToken(), request.getOs());
        return ResponseEntity.ok().build();
    }

    // 로그아웃 시 비활성화
    @PostMapping("/deactivate")
    public ResponseEntity<Void> deactivate(@RequestBody DeactivateRequest request) {
        deviceTokenService.deactivate(request.getToken());
        return ResponseEntity.ok().build();
    }
}
