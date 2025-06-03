package project.votebackend.controller.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.votebackend.dto.vote.LoadVoteDto;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.vote.VoteLoadService;
import project.votebackend.service.vote.VoteService;
import project.votebackend.util.PageResponseUtil;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vote")
public class VoteLoadController {

    private final VoteLoadService voteLoadService;

    //메인페이지 투표 불러오기 (자신이 작성한, 자신이 선택한 카테고리, 자신이 팔로우한 사람의 글)
    @GetMapping("/load-main-page-votes")
    public ResponseEntity<Map<String, Object>> loadMainPageVotes(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LoadVoteDto> votePage = voteLoadService.getMainPageVotes(userDetails.getId(), pageable);
        return ResponseEntity.ok(PageResponseUtil.toResponse(votePage));
    }

    //단일 투표 불러오기
    @GetMapping("/{voteId}")
    public ResponseEntity<LoadVoteDto> getVoteById(
            @PathVariable Long voteId,
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        LoadVoteDto voteDto = voteLoadService.getVoteById(voteId, userDetails.getId());
        return ResponseEntity.ok(voteDto);
    }

    //특정 카테고리의 게시물 불러오기
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getVotesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Page<LoadVoteDto> votePage = voteLoadService.getVotesByCategorySortedByLike(categoryId, page, size, userDetails.getUsername());
        return ResponseEntity.ok(PageResponseUtil.toResponse(votePage));
    }
}
