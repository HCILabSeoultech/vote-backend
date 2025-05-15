package project.votebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import project.votebackend.domain.Comment;

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
        boolean isLiked = comment.getCommentLikes().stream()
                .anyMatch(like -> like.getUser().getUserId().equals(currentUserId));

        // Comment 엔티티의 정보를 기반으로 CommentResponse 객체 생성
        return CommentResponse.builder()
                .id(comment.getCommentId())                                       // 댓글 ID
                .userId(comment.getUser().getUserId())                            // 작성자 ID
                .username(comment.getUser().getUsername())                        // 작성자 닉네임
                .name(comment.getUser().getName())                                // 작성자 이름 (필요 시 사용)
                .content(comment.getContent())                                    // 댓글 내용
                .createdAt(comment.getCreatedAt())                                // 작성 시간
                .profileImage(comment.getUser().getProfileImage())                // 작성자 프로필 이미지
                .likeCount(comment.getLikeCount())                                // 댓글 좋아요 수
                .parentId(comment.getParent() != null ?                           // 부모 댓글 ID (null이면 최상위 댓글)
                        comment.getParent().getCommentId() : null)
                .isLiked(isLiked)                                                 // 현재 유저가 좋아요 눌렀는지
                .replies(null)                                                    // 대댓글 없음
                .build();
    }

    // 대댓글 포함 버전: 부모 댓글 + 자식 댓글 리스트를 함께 처리
    public static CommentResponse fromEntityWithReplies(Comment parent, List<Comment> replies, Long currentUserId) {
        // 현재 사용자가 부모 댓글에 좋아요를 눌렀는지 여부 확인
        boolean isLiked = parent.getCommentLikes().stream()
                .anyMatch(like -> like.getUser().getUserId().equals(currentUserId));

        // 부모 댓글 정보를 기반으로 CommentResponse 생성 (replies 포함)
        return CommentResponse.builder()
                .id(parent.getCommentId())                                        // 부모 댓글 ID
                .userId(parent.getUser().getUserId())                             // 작성자 ID
                .username(parent.getUser().getUsername())                         // 작성자 닉네임
                .content(parent.getContent())                                     // 댓글 내용
                .createdAt(parent.getCreatedAt())                                 // 작성 시간
                .profileImage(parent.getUser().getProfileImage())                 // 작성자 프로필 이미지
                .likeCount(parent.getLikeCount())                                 // 좋아요 수
                .parentId(null)                                                   // 부모 댓글이므로 parentId는 null
                .isLiked(isLiked)                                                 // 현재 유저가 좋아요 눌렀는지
                .replies(replies.stream()                                         // 자식 댓글들을 다시 fromEntity로 변환
                        .map(reply -> fromEntity(reply, currentUserId))
                        .collect(Collectors.toList()))
                .build();
    }

}
