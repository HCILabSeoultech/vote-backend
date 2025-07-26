package project.votebackend.controller.comment;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.service.comment.CommentLikeService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment-like")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    //댓글 좋아요
    @PostMapping("/{commentId}")
    @Operation(summary = "댓글 좋아요 API", description = "댓글에 좋아요를 누릅니다(토글 형식)")
    public ResponseEntity<Map<String, Object>> like(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isLiked = commentLikeService.like(commentId, userDetails.getUsername());
        long likeCount = commentLikeService.getLikeCount(commentId);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", likeCount);

        return ResponseEntity.ok(result);
    }
}
