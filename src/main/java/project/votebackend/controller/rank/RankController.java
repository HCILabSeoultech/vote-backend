package project.votebackend.controller.rank;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.user.UserMonthlyRankDto;
import project.votebackend.service.rank.RankRedisService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("rank")
public class RankController {

    private final RankRedisService rankRedisService;

    @GetMapping("/monthly")
    public ResponseEntity<List<UserMonthlyRankDto>> getMonthlyRank(
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(rankRedisService.getMonthlyRank(limit));
    }
}
