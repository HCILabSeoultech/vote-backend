package project.votebackend.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.votebackend.domain.user.User;
import project.votebackend.dto.user.*;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.user.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    //마이페이지 조회
    @GetMapping("/mypage")
    @Operation(summary = "마이페이지 조회 API", description = "마이페이지를 조회합니다.")
    public ResponseEntity<UserPageDto> getMyPage(
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        UserPageDto dto = userService.getMyPage(userDetails.getId());
        return ResponseEntity.ok(dto);
    }

    //다른 사용자 조회
    @GetMapping("/{userId}")
    @Operation(summary = "다른 사람 조회 API", description = "다른 사람의 페이지를 조회합니다.")
    public ResponseEntity<OtherUserPageDto> getUserPage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        OtherUserPageDto otherUserPage = userService.getUserPage(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(otherUserPage);
    }

    //회원정보 수정
    @PatchMapping("/update")
    @Operation(summary = "회원정보 수정 API", description = "회원정보를 수정합니다.")
    public ResponseEntity<UserResponseDto> updateUserInfo(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @RequestBody @Valid UserUpdateDto dto
    ) {
        UserResponseDto updatedUser = userService.updateUser(userDetails.getId(), dto);
        return ResponseEntity.ok(updatedUser);
    }

    //내 정보 가져오기
    @GetMapping("/info")
    @Operation(summary = "회원정보 수정 시 나의 정보 조회 API", description = "회원정보 수정 시 나의 정보를 조회합니다.")
    public ResponseEntity<UserInfoDto> getUserInfo(@AuthenticationPrincipal CustumUserDetails userDetails) {
        UserInfoDto userInfo = userService.getUserInfo(userDetails.getId());
        return ResponseEntity.ok(userInfo);
    }

    //임시저장 설명 다시보기 여부
    @GetMapping("/getDraftHelpVersionSeen")
    @Operation(summary = "임시저장 설명 다시보기 여부 조회 API", description = "임시저장시 설명 다시보기 여부를 조회합니다.")
    public ResponseEntity<Boolean> getDraftHelpVersionSeen(@AuthenticationPrincipal CustumUserDetails userDetails) {
        boolean draftHelpVersionSeen = userService.getDraftHelpVersionSeen(userDetails.getId());
        return ResponseEntity.ok(draftHelpVersionSeen);
    }
}
