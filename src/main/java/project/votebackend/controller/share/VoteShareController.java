package project.votebackend.controller.share;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.vote.LoadVoteDto;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.vote.VoteService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/share/vote")
public class VoteShareController {

    private final VoteService voteService;

    //투표 링크 복사
    @GetMapping("/link/{voteId}")
    public ResponseEntity<?> getShareLink(@PathVariable Long voteId) {
        String baseUrl = "https://votey-backend.p-e.kr/share/vote/";
        return ResponseEntity.ok(Map.of("shareUrl", baseUrl + voteId));
    }

    //공유용으로 사용되기 때문에 null이 가능
    @GetMapping("/{voteId}")
    public ResponseEntity<LoadVoteDto> getVoteById(
            @PathVariable Long voteId,
            @AuthenticationPrincipal CustumUserDetails userDetails // null일 수 있음
    ) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        LoadVoteDto voteDto = voteService.getVoteById(voteId, userId);
        return ResponseEntity.ok(voteDto);
    }
}
