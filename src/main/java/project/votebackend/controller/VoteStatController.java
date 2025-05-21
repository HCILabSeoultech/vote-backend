package project.votebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.DailyVoteStatDto;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.VoteStatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/stats")
public class VoteStatController {

    private final VoteStatService voteStatService;

    //최근 일주일 투표 생성, 참여 스탯 조회
    @GetMapping("/week")
    public ResponseEntity<List<DailyVoteStatDto>> getWeeklyStats(@AuthenticationPrincipal CustumUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<DailyVoteStatDto> stats = voteStatService.getLast7DaysStats(userId);
        return ResponseEntity.ok(stats);
    }
}
