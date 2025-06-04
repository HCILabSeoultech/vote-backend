package project.votebackend.controller.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.votebackend.dto.comment.CommentRequest;
import project.votebackend.dto.comment.CommentResponse;
import project.votebackend.service.comment.CommentService;
import project.votebackend.util.PageResponseUtil;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/{voteId}")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long voteId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentResponse response = commentService.createComment(
                voteId,
                request.getContent(),
                userDetails.getUsername(),
                request.getParentId()
        );

        return ResponseEntity.ok(response);
    }

    // 댓글 조회
    @GetMapping("/{voteId}")
    public ResponseEntity<Map<String, Object>> getComments(
            @PathVariable Long voteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId
    ) {
        Page<CommentResponse> comments = commentService.getComments(voteId, userId, page, size);
        return ResponseEntity.ok(PageResponseUtil.toResponse(comments));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> editComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentResponse response = commentService.editComment(
                commentId,
                request.getContent(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok(response);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok("success");
    }
}
