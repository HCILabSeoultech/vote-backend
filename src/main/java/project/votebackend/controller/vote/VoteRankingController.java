package project.votebackend.controller.vote;

import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "전체 득표순 투표 조회 API", description = "득표가 많은 순으로 투표를 조회합니다.")
    public List<VoteSummaryDto> getPopularVotes(@RequestParam VoteStatusType status,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return voteRankingService.getVotesSortedByTotalVotes(status, page, size);
    }

//    //오늘 득표순 정렬
//    @GetMapping("/today")
//    public List<VoteSummaryDto> getTodayPopularVotes(@RequestParam(defaultValue = "0") int page,
//                                                     @RequestParam(defaultValue = "20") int size) {
//        return voteRankingService.getVotesSortedByTodayVotes(page, size);
//    }

    //좋아요순 정렬
    @GetMapping("/likes")
    @Operation(summary = "전체 좋아요순 투표 조회 API", description = "좋아요가 많은 순으로 투표를 조회합니다.")

    public List<VoteSummaryDto> getMostLikedVotes(@RequestParam VoteStatusType status,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return voteRankingService.getVotesSortedByLikes(status, page, size);
    }

    //댓글순 정렬
    @GetMapping("/comments")
    @Operation(summary = "전체 댓글순 투표 조회 API", description = "댓글이 많은 순으로 투표를 조회합니다.")

    public List<VoteSummaryDto> getMostCommentedVotes(@RequestParam VoteStatusType status,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return voteRankingService.getVotesSortedByComments(status, page, size);
    }

//    //급상승 정렬
//    @GetMapping("/trending")
//    public List<TrendingVoteDto> getTrendingVotes(@RequestParam(defaultValue = "0") int page,
//                                                  @RequestParam(defaultValue = "20") int size) {
//        return voteRankingService.getTrendingVotes(page, size);
//    }

    //급상승 정렬
    @GetMapping("/trending")
    @Operation(summary = "전체 인기순 투표 조회 API", description = "인기가 많은 순으로 투표를 조회합니다.")
    public List<VoteSummaryDto> getTrendingVotes(@RequestParam VoteStatusType status,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return voteRankingService.getTrendingVotes(status, page, size);
    }
}
