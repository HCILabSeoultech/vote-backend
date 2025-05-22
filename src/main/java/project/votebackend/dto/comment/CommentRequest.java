package project.votebackend.dto.comment;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private Long parentId;
}
