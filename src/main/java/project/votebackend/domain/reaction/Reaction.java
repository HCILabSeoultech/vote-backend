package project.votebackend.domain.reaction;

import jakarta.persistence.*;
import lombok.*;
import project.votebackend.domain.BaseEntity;
import project.votebackend.domain.user.User;
import project.votebackend.domain.vote.Vote;
import project.votebackend.type.ReactionType;

@Entity
@Table(
        name = "reaction",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "vote_id", "reaction"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reaction extends BaseEntity {
    @Id
    @GeneratedValue
    private Long reactionId;

    @ManyToOne
    @JoinColumn(name = "vote_id")
    private Vote vote;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ReactionType reaction;
}
