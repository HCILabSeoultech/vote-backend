package project.votebackend.controller.user;

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
    public ResponseEntity<UserPageDto> getMyPage(
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        UserPageDto dto = userService.getMyPage(userDetails.getId());
        return ResponseEntity.ok(dto);
    }

    //다른 사용자 조회
    @GetMapping("/{userId}")
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
    public ResponseEntity<UserResponseDto> updateUserInfo(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @RequestBody @Valid UserUpdateDto dto
    ) {
        UserResponseDto updatedUser = userService.updateUser(userDetails.getId(), dto);
        return ResponseEntity.ok(updatedUser);
    }

    //내 정보 가져오기
    @GetMapping("/info")
    public ResponseEntity<UserInfoDto> getUserInfo(@AuthenticationPrincipal CustumUserDetails userDetails) {
        UserInfoDto userInfo = userService.getUserInfo(userDetails.getId());
        return ResponseEntity.ok(userInfo);
    }
}
