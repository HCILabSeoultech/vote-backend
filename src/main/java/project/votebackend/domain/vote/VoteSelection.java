package project.votebackend.domain.vote;

import jakarta.persistence.*;
import lombok.*;
import project.votebackend.domain.BaseEntity;
import project.votebackend.domain.user.User;

@Entity
@Table(name = "vote_selections")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteSelection extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long selectionsId;

    @ManyToOne
    @JoinColumn(name = "vote_id")
    private Vote vote;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private VoteOption option;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
