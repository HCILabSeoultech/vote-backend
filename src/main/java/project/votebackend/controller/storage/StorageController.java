package project.votebackend.controller.storage;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.vote.VoteSummaryDto;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.storage.StorageService;
import project.votebackend.util.PageResponseUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageController {

    private final StorageService storageService;

    //투표한 게시물 불러오기
    @GetMapping("/voted")
    @Operation(summary = "투표한 게시물 조회 API", description = "내가 투표한 게시물을 조회합니다.")
    public List<VoteSummaryDto> getVotedPosts(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return storageService.getVotedPosts(userDetails.getId(), pageable);
    }

    //북마크한 게시물 불러오기
    @GetMapping("/bookmarked")
    @Operation(summary = "북마크한 게시물 조회 API", description = "내가 북마크한 게시물을 조회합니다.")
    public List<VoteSummaryDto> getBookmarkedPosts(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return storageService.getBookmarkedPosts(userDetails.getId(), pageable);
    }

    //내가 작성한 게시물 불러오기
    @GetMapping("/created")
    @Operation(summary = "작성한 게시물 조회 API", description = "내가 작성한 게시물을 조회합니다.")
    public List<VoteSummaryDto> getCreatedPosts(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return storageService.getCreatedPosts(userDetails.getId(), pageable);
    }

    @GetMapping("/drafts")
    @Operation(summary = "임시 저장한 게시물 조회 API", description = "내가 임시 저장한 게시물(DRAFT 상태)을 조회합니다.")
    public List<VoteSummaryDto> getDraftPosts(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return storageService.getDraftPosts(userDetails.getId(), pageable);
    }
}
