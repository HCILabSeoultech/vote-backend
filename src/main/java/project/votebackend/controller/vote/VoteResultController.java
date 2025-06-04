package project.votebackend.controller.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.voteResult.VoteResultStatisticsDto;
import project.votebackend.service.vote.VoteResultService;

import java.util.Map;

@RestController
@RequestMapping("/result")
@RequiredArgsConstructor
public class VoteResultController {

    private final VoteResultService voteResultService;

    //성별 기준 분석
    @GetMapping("/{voteId}/statistics/gender")
    public ResponseEntity<Map<String, VoteResultStatisticsDto>> getGenderStats(@PathVariable Long voteId) {
        return ResponseEntity.ok(voteResultService.getGenderStats(voteId));
    }

    //연령 기준 분석
    @GetMapping("/{voteId}/statistics/age")
    public ResponseEntity<Map<String, VoteResultStatisticsDto>> getAgeStats(@PathVariable Long voteId) {
        return ResponseEntity.ok(voteResultService.getAgeStats(voteId));
    }

    //지역 기준 분석
    @GetMapping("/{voteId}/statistics/region")
    public ResponseEntity<Map<String, VoteResultStatisticsDto>> getRegionStats(@PathVariable Long voteId) {
        return ResponseEntity.ok(voteResultService.getRegionStats(voteId));
    }
}
