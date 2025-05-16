package project.votebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.LoadVoteDto;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.StorageService;
import project.votebackend.util.PageResponseUtil;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageController {

    private final StorageService storageService;

    //투표한 게시물 불러오기
    @GetMapping("/voted")
    public ResponseEntity<Map<String, Object>> getVotedPosts(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponseUtil.toResponse(storageService.getVotedPosts(userDetails.getId(), pageable)));
    }

    //좋아요한 게시물 불러오기
    @GetMapping("/liked")
    public ResponseEntity<Map<String, Object>> getLikedPosts(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponseUtil.toResponse(storageService.getLikedPosts(userDetails.getId(), pageable)));
    }

    //북마크한 게시물 불러오기
    @GetMapping("/bookmarked")
    public ResponseEntity<Map<String, Object>> getBookmarkedPosts(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponseUtil.toResponse(storageService.getBookmarkedPosts(userDetails.getId(), pageable)));
    }
}
