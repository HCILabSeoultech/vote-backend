package project.votebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.votebackend.domain.Vote;
import project.votebackend.dto.*;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.VoteService;
import project.votebackend.util.PageResponseUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vote")
public class VoteController {

    private final VoteService voteService;

    //투표 생성
    @PostMapping("/create")
    public ResponseEntity<?> createVote(
            @RequestBody CreateVoteRequest request,
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        Vote created = voteService.createVote(request, userDetails.getId());
        CreateVoteResponse response = new CreateVoteResponse("success", created.getVoteId());
        return ResponseEntity.ok(response);
    }

    // 투표 재업로드
    @PostMapping("/{voteId}/reupload")
    public ResponseEntity<?> reuploadVote(@PathVariable Long voteId,
                                          @RequestBody VoteReuploadRequest request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        Long newVoteId = voteService.reuploadVote(voteId, request.getFinishTime(), userDetails.getUsername());
        return ResponseEntity.ok(Map.of("newVoteId", newVoteId));
    }

    // 투표 삭제
    @DeleteMapping("/{voteId}")
    public ResponseEntity<?> deleteVote(@PathVariable Long voteId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        voteService.deleteVote(voteId, userDetails.getUsername());
        return ResponseEntity.ok("success");
    }

    //메인페이지 투표 불러오기 (자신이 작성한, 자신이 선택한 카테고리, 자신이 팔로우한 사람의 글)
    @GetMapping("/load-main-page-votes")
    public ResponseEntity<Map<String, Object>> loadMainPageVotes(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LoadVoteDto> votePage = voteService.getMainPageVotes(userDetails.getId(), pageable);
        return ResponseEntity.ok(PageResponseUtil.toResponse(votePage));
    }

    //단일 투표 불러오기
    @GetMapping("/{voteId}")
    public ResponseEntity<LoadVoteDto> getVoteById(
            @PathVariable Long voteId,
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        LoadVoteDto voteDto = voteService.getVoteById(voteId, userDetails.getId());
        return ResponseEntity.ok(voteDto);
    }

    //좋아요 수 상위 게시물
    //추후 수정해야할 api
    @GetMapping("/top-liked")
    public ResponseEntity<List<LoadVoteDto>> getTopLikedVotes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "30") int size) {

        List<LoadVoteDto> result = voteService.getTopLikedVotes(userDetails.getUsername(), size);
        return ResponseEntity.ok(result);
    }

    //특정 카테고리의 게시물 불러오기
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getVotesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Page<LoadVoteDto> votePage = voteService.getVotesByCategorySortedByLike(categoryId, page, size, userDetails.getUsername());
        return ResponseEntity.ok(PageResponseUtil.toResponse(votePage));
    }
}
