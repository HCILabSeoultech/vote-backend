package project.votebackend.controller.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.votebackend.domain.vote.Vote;
import project.votebackend.dto.vote.CreateVoteRequest;
import project.votebackend.dto.vote.CreateVoteResponse;
import project.votebackend.dto.vote.VoteReuploadRequest;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.vote.VoteService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vote")
public class VoteController {

    private final VoteService voteService;

    //투표 생성
    @PostMapping("/create")
    public ResponseEntity<?> createVote(
            @RequestBody CreateVoteRequest request,
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        Vote created = voteService.createVote(request, userDetails.getId());
        CreateVoteResponse response = new CreateVoteResponse("success", created.getVoteId());
        return ResponseEntity.ok(response);
    }

    // 투표 재업로드
    @PostMapping("/{voteId}/reupload")
    public ResponseEntity<?> reuploadVote(@PathVariable Long voteId,
                                          @RequestBody VoteReuploadRequest request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        Long newVoteId = voteService.reuploadVote(voteId, request.getFinishTime(), userDetails.getUsername());
        return ResponseEntity.ok(Map.of("newVoteId", newVoteId));
    }

    // 투표 삭제
    @DeleteMapping("/{voteId}")
    public ResponseEntity<?> deleteVote(@PathVariable Long voteId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        voteService.deleteVote(voteId, userDetails.getUsername());
        return ResponseEntity.ok("success");
    }
}
