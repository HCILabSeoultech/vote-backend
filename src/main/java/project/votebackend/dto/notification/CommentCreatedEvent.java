package project.votebackend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentCreatedEvent {
    private final Long voteId;          // 글(투표) ID
    private final Long commentId;       // 생성된 댓글 ID
    private final Long commenterId;     // 댓글 작성자 ID
    private final Long postAuthorId;    // 글 작성자 ID
    private final Long parentCommentId; // 대댓글이면 부모 댓글 ID (없으면 null)
    private final Long parentAuthorId;  // 부모 댓글 작성자 ID (없으면 null)
}
