package project.votebackend.domain.reaction;

import jakarta.persistence.*;
import lombok.*;
import project.votebackend.domain.BaseEntity;
import project.votebackend.domain.article.Cluster;
import project.votebackend.domain.user.User;

@Entity
@Table(name = "news_bookmark")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NewsBookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookmarkId;

    @ManyToOne
    @JoinColumn(name = "cluster_id")
    private Cluster cluster;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
