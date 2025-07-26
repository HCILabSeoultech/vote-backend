package project.votebackend.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.client.KakaoMapClient;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/location")
public class LocationController {

    private final KakaoMapClient kakaoMapClient;

    @GetMapping("/verify")
    @Operation(summary = "위치 인증 API", description = "현재의 위치를 반환합니다(ex.서울시)")
    public ResponseEntity<Map<String, String>> verifyLocation(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        String region = kakaoMapClient.getTopRegionByCoords(lat, lng);
        return ResponseEntity.ok(Map.of("region", region));
    }
}
