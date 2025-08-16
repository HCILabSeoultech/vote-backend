package project.votebackend.domain.article;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cluster")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String subtitle1;

    @Column(columnDefinition = "TEXT")
    private String subtitle2;

    @Column(columnDefinition = "TEXT")
    private String subtitle3;

    @Column(columnDefinition = "TEXT")
    private String content1;

    @Column(columnDefinition = "TEXT")
    private String content2;

    @Column(columnDefinition = "TEXT")
    private String content3;

    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Article> articles = new ArrayList<>();

}
