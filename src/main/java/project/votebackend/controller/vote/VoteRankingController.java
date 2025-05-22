package project.votebackend.controller.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.vote.TrendingVoteDto;
import project.votebackend.dto.vote.VoteSummaryDto;
import project.votebackend.service.vote.VoteRankingService;
import project.votebackend.type.VoteStatusType;

import java.util.List;

@RestController
@RequestMapping("/rank")
@RequiredArgsConstructor
public class VoteRankingController {

    private final VoteRankingService voteRankingService;

    //전체 득표순 정렬
    @GetMapping("/popular")
    public List<VoteSummaryDto> getPopularVotes(@RequestParam VoteStatusType status,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return voteRankingService.getVotesSortedByTotalVotes(status, page, size);
    }

    //오늘 득표순 정렬
    @GetMapping("/today")
    public List<VoteSummaryDto> getTodayPopularVotes(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return voteRankingService.getVotesSortedByTodayVotes(page, size);
    }

    //댓글순 정렬
    @GetMapping("/comments")
    public List<VoteSummaryDto> getMostCommentedVotes(@RequestParam VoteStatusType status,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return voteRankingService.getVotesSortedByComments(status, page, size);
    }

    //급상승 정렬
    @GetMapping("/trending")
    public List<TrendingVoteDto> getTrendingVotes(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return voteRankingService.getTrendingVotes(page, size);
    }
}
