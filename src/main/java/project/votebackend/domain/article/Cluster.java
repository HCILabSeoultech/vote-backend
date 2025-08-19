package project.votebackend.domain.article;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.votebackend.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cluster")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cluster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cluster_id")
    private Long id;

    @Column
    private String imageUrl;

    @Column
    private String sourceArticleUrl;

    @Column
    private String sourceOriginalArticleUrl;

    @Column
    private String title;

    @Column
    private String content1;

    @Column
    private String content2;

    @Column
    private String content3;

    @Column
    private String content4;

    @Column
    private String subtitle1;

    @Column
    private String subtitle2;

    @Column
    private String subtitle3;

    @Column
    private String subtitle4;

    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Article> articles = new ArrayList<>();
}
