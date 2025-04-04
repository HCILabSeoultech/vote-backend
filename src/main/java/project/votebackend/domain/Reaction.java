package project.votebackend.domain;

import jakarta.persistence.*;
import lombok.*;
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
public class Reaction extends BaseEntity{
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
