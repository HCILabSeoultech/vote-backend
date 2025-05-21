package project.votebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.votebackend.exception.AuthException;
import project.votebackend.repository.UserRepository;
import project.votebackend.service.FollowService;
import project.votebackend.type.ErrorCode;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;
    private final UserRepository userRepository;

    // 팔로우
    @PostMapping
    public ResponseEntity<?> follow(@RequestBody Map<String, Long> body,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long followingId = body.get("followingId");

        Long followId = followService.follow(userDetails.getUsername(), followingId);
        return ResponseEntity.ok(Map.of("followId", followId, "message", "팔로우 성공"));
    }

    // 언팔로우
    @DeleteMapping
    public ResponseEntity<?> unfollow(@RequestBody Map<String, Long> body,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        Long followingId = body.get("followingId");

        followService.unfollow(userDetails.getUsername(), followingId);
        return ResponseEntity.ok(Map.of("message", "팔로우 취소 완료"));
    }

    // 팔로우 여부 확인
    @GetMapping("/check")
    public ResponseEntity<?> checkFollow(@RequestParam Long followingId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        boolean isFollowing = followService.isFollowing(userDetails.getUsername(), followingId);
        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }
}
