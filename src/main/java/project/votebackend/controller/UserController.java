package project.votebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.votebackend.dto.UserPageDto;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    //마이페이지 조회
    @GetMapping("/mypage")
    public ResponseEntity<UserPageDto> getMyPage(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        UserPageDto dto = userService.getMyPage(userDetails.getId(), pageable);
        return ResponseEntity.ok(dto);
    }

    //다른 사용자 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserPageDto> getUserPage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UserPageDto userPage = userService.getUserPage(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(userPage);
    }
}
