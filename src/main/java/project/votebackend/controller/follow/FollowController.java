package project.votebackend.controller.follow;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.votebackend.dto.follow.FollowUserDto;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.follow.FollowListService;
import project.votebackend.service.follow.FollowService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;
    private final FollowListService followListService;

    // 팔로우
    @PostMapping
    @Operation(summary = "팔로우 API", description = "다른 사람을 팔로우합니다.")
    public ResponseEntity<?> follow(@RequestBody Map<String, Long> body,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long followingId = body.get("followingId");

        Long followId = followService.follow(userDetails.getUsername(), followingId);
        return ResponseEntity.ok(Map.of("followId", followId, "message", "팔로우 성공"));
    }

    // 언팔로우
    @DeleteMapping
    @Operation(summary = "언팔로우 API", description = "다른 사람을 언팔로우합니다.")
    public ResponseEntity<?> unfollow(@RequestBody Map<String, Long> body,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        Long followingId = body.get("followingId");

        followService.unfollow(userDetails.getUsername(), followingId);
        return ResponseEntity.ok(Map.of("message", "팔로우 취소 완료"));
    }

    // 팔로우 여부 확인
    @GetMapping("/check")
    @Operation(summary = "팔로우 체크 API", description = "다른 사람을 팔로우했는지 확인합니다.")
    public ResponseEntity<?> checkFollow(@RequestParam Long followingId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        boolean isFollowing = followService.isFollowing(userDetails.getUsername(), followingId);
        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }

    // 나를 팔로우한 사람 목록 조회
    @GetMapping("/followers")
    @Operation(summary = "나의 팔로워 목록 확인 API", description = "나를 팔로우한 사람의 목록을 확인합니다.")
    public ResponseEntity<List<FollowUserDto>> getMyFollowers(@AuthenticationPrincipal CustumUserDetails userDetails) {
        Long myId = userDetails.getId();
        return ResponseEntity.ok(followListService.getFollowers(myId));
    }

    // 내가 팔로우한 사람 목록 조회
    @GetMapping("/followings")
    @Operation(summary = "나의 팔로잉 목록 확인 API", description = "내가 팔로우한 사람의 목록을 확인합니다.")
    public ResponseEntity<List<FollowUserDto>> getMyFollowings(@AuthenticationPrincipal CustumUserDetails userDetails) {
        Long myId = userDetails.getId();
        return ResponseEntity.ok(followListService.getFollowings(myId));
    }

    // 특정 사용자의 팔로워 목록 조회
    @GetMapping("/{userId}/followers")
    @Operation(summary = "특정 사용자의 팔로워 목록 확인 API", description = "특정 사용자가 팔로우한 사람의 목록을 확인합니다.")
    public ResponseEntity<List<FollowUserDto>> getUserFollowers(@PathVariable Long userId,
                                                                @AuthenticationPrincipal CustumUserDetails userDetails) {
        return ResponseEntity.ok(followListService.getFollowersOfUser(userId, userDetails.getId()));
    }

    // 특정 사용자의 팔로잉 목록 조회
    @GetMapping("/{userId}/followings")
    @Operation(summary = "특정 사용자의 팔로잉 목록 확인 API", description = "특정 사용자를 팔로우한 사람의 목록을 확인합니다.")
    public ResponseEntity<List<FollowUserDto>> getUserFollowings(@PathVariable Long userId,
                                                                 @AuthenticationPrincipal CustumUserDetails userDetails) {
        return ResponseEntity.ok(followListService.getFollowingsOfUser(userId, userDetails.getId()));
    }
}
