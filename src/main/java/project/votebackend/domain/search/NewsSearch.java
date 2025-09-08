package project.votebackend.domain.search;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.votebackend.domain.BaseEntity;
import project.votebackend.domain.user.User;

@Entity
@Table(name = "news_search")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NewsSearch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long searchId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String keyword;
}
