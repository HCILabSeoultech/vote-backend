package project.votebackend.domain.user;

import jakarta.persistence.*;
import lombok.*;
import project.votebackend.domain.BaseEntity;
import project.votebackend.domain.category.Category;

@Entity
@Table(name = "user_interest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interestsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id")
    private Category category;
}
