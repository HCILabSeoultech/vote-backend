package project.votebackend.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import project.votebackend.domain.comment.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String content;
    private LocalDateTime createdAt;
    private String profileImage;
    private int likeCount;
    private Long parentId;

    @JsonProperty("isLiked")
    private boolean isLiked;
    private List<CommentResponse> replies;

    // 단일 댓글을 CommentResponse DTO로 변환하는 메서드
    public static CommentResponse fromEntity(Comment comment, Long currentUserId) {
        // 현재 사용자가 해당 댓글에 좋아요를 눌렀는지 여부 확인
        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = comment.getCommentLikes().stream()
                    .anyMatch(like -> like.getUser().getUserId().equals(currentUserId));
        }

        // Comment 엔티티의 정보를 기반으로 CommentResponse 객체 생성
        return CommentResponse.builder()
                .id(comment.getCommentId())
                .userId(comment.getUser().getUserId())
                .username(comment.getUser().getUsername())
                .name(comment.getUser().getName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .profileImage(comment.getUser().getProfileImage())
                .likeCount(comment.getLikeCount())
                .parentId(comment.getParent() != null ? comment.getParent().getCommentId() : null)
                .isLiked(isLiked)
                .replies(null)
                .build();
    }

    // 대댓글 포함 버전: 부모 댓글 + 자식 댓글 리스트를 함께 처리
    public static CommentResponse fromEntityWithReplies(Comment parent, List<Comment> replies, Long currentUserId) {
        // 현재 사용자가 부모 댓글에 좋아요를 눌렀는지 여부 확인
        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = parent.getCommentLikes().stream()
                    .anyMatch(like -> like.getUser().getUserId().equals(currentUserId));
        }

        // 부모 댓글 정보를 기반으로 CommentResponse 생성 (replies 포함)
        return CommentResponse.builder()
                .id(parent.getCommentId())
                .userId(parent.getUser().getUserId())
                .username(parent.getUser().getUsername())
                .name(parent.getUser().getName())
                .content(parent.getContent())
                .createdAt(parent.getCreatedAt())
                .profileImage(parent.getUser().getProfileImage())
                .likeCount(parent.getLikeCount())
                .parentId(null)
                .isLiked(isLiked)
                .replies(replies.stream()
                        .map(reply -> fromEntity(reply, currentUserId))
                        .collect(Collectors.toList()))
                .build();
    }
}
