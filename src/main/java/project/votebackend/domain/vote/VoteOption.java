package project.votebackend.domain.vote;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vote_option")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoteOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @ManyToOne
    @JoinColumn(name = "vote_id")
    private Vote vote;

    private String option;
    private String optionImage;

    public VoteOption(String option, Vote vote) {
        this.option = option;
        this.vote = vote;
    }
}