package project.votebackend.controller.search;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    //투표 검색어 입력
    @GetMapping("/vote")
    public List<VoteDocument> searchVotes(@RequestParam("keyword") String keyword) throws IOException {
        return searchService.searchVotes(keyword);
    }

    //유저 검색어 입력
    @GetMapping("/user")
    public List<UserDocument> searchUsers(@RequestParam("keyword") String keyword) throws IOException {
        return searchService.searchUsers(keyword);
    }
}
