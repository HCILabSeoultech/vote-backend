package project.votebackend.domain.comment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.votebackend.domain.BaseEntity;
import project.votebackend.domain.user.User;

@Entity
@Table(name = "comment_like")
@Setter
@Getter
public class CommentLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;
}
