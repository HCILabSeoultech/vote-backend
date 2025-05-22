package project.votebackend.domain.vote;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vote_image")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoteImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteImageId;

    @ManyToOne
    @JoinColumn(name = "vote_id")
    private Vote vote;

    private String imageUrl;


    public VoteImage(String imageUrl, Vote newVote) {
        this.imageUrl = imageUrl;
        this.vote = newVote;
    }
}
