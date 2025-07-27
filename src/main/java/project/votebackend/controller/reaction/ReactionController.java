package project.votebackend.controller.reaction;

import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "좋아요 API", description = "게시글에 좋아요를 누릅니다(토글 형식).")
    public ResponseEntity<?> like(
            @RequestParam Long voteId,
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        reactionService.like(voteId, userDetails.getId());
        return ResponseEntity.ok("success");
    }

    //북마크 처리
    @PostMapping("/bookmark")
    @Operation(summary = "북마크 API", description = "게시글에 북마크를 누릅니다(토글 형식).")
    public ResponseEntity<?> bookmark(
            @RequestParam Long voteId,
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        reactionService.bookmark(voteId, userDetails.getId());
        return ResponseEntity.ok("success");
    }

}
