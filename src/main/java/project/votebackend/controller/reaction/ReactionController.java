package project.votebackend.controller.reaction;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.reaction.ReactionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reaction")
public class ReactionController {

    private final ReactionService reactionService;

    //좋아요 처리
    @PostMapping("/like")
    public ResponseEntity<?> like(
            @RequestParam Long voteId,
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        reactionService.like(voteId, userDetails.getId());
        return ResponseEntity.ok("success");
    }

    //북마크 처리
    @PostMapping("/bookmark")
    public ResponseEntity<?> bookmark(
            @RequestParam Long voteId,
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        reactionService.bookmark(voteId, userDetails.getId());
        return ResponseEntity.ok("success");
    }

}
