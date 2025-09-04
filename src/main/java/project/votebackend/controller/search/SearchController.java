package project.votebackend.controller.search;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.votebackend.dto.article.ClusterSummaryDto;
import project.votebackend.dto.vote.VoteSearchResponse;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.search.SearchService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    //쿼리문을 이용한 투표 검색
    @GetMapping("/vote")
    @Operation(summary = "게시글 검색 API", description = "게시글을 제목으로 검색합니다.")
    public Page<VoteSearchResponse> searchVotes(
            @RequestParam("keyword") String keyword,
            @PageableDefault(size = 20, page = 0) Pageable pageable
    ) {
        return searchService.searchVotes(keyword, pageable);
    }

    //뉴스 검색
    @GetMapping("/news")
    @Operation(summary = "뉴스 검색 API", description = "뉴스를 제목으로 검색합니다.")
    public Page<ClusterSummaryDto> searchNews(
            @RequestParam String keyword,
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @AuthenticationPrincipal CustumUserDetails userDetails
            ) {
        return searchService.searchNews(keyword, pageable, userDetails.getId());
    }
}
