package project.votebackend.domain.article;

import jakarta.persistence.*;
import lombok.*;
import project.votebackend.domain.BaseEntity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "article")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id")
    private Cluster cluster;

    @Column
    private String url;

    @Column
    private String originalUrl;

    @Column
    private String title;

    @Column
    private String publisher;

    @Column
    private LocalDateTime publishedAt;
}
