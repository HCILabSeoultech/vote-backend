package project.votebackend.controller.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.vote.VoteSearchResponse;
import project.votebackend.elasticSearch.UserDocument;
import project.votebackend.elasticSearch.VoteDocument;
import project.votebackend.service.elasticsearch.SearchService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    //쿼리문을 이용한 투표 검색
    @GetMapping("/vote")
    public Page<VoteSearchResponse> searchVotes(
            @RequestParam("keyword") String keyword,
            @PageableDefault(size = 20, page = 0) Pageable pageable
    ) {
        return searchService.searchVotes(keyword, pageable);
    }

//    //투표 검색어 입력
//    @GetMapping("/vote")
//    public List<VoteDocument> searchVotes(@RequestParam("keyword") String keyword) throws IOException {
//        return searchService.searchVotes(keyword);
//    }

    //유저 검색어 입력
    @GetMapping("/user")
    public List<UserDocument> searchUsers(@RequestParam("keyword") String keyword) throws IOException {
        return searchService.searchUsers(keyword);
    }
}
