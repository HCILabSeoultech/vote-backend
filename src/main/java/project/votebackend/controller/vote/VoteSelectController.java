package project.votebackend.controller.vote;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.voteSelect.VoteSelectRequest;
import project.votebackend.dto.voteSelect.VoteSelectResponse;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.vote.VoteSelectService;

@RestController
@RequestMapping("/vote")
@RequiredArgsConstructor
public class VoteSelectController {

    private final VoteSelectService voteSelectService;

    //투표 선택
    @PostMapping("/select")
    @Operation(summary = "투표 참여 API", description = "투표에 참여합니다.")
    public ResponseEntity<?> voteSelect(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @RequestBody VoteSelectRequest request
    ) {
        Long userId = userDetails.getId();
        VoteSelectResponse response = voteSelectService.saveVoteSelection(userId, request.getVoteId(), request.getOptionId());
        return ResponseEntity.ok(response);
    }

    //투표 취소
    @PostMapping("/cancel")
    @Operation(summary = "투표 취소 API", description = "투표를 취소합니다.")
    public ResponseEntity<?> cancelVote(
            @AuthenticationPrincipal CustumUserDetails userDetails,
            @RequestBody VoteSelectRequest request
    ) {
        Long userId = userDetails.getId();
        voteSelectService.cancelVoteSelection(userId, request.getVoteId());
        return ResponseEntity.ok("투표가 취소되었습니다.");
    }
}
