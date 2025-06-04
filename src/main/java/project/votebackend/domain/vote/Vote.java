package project.votebackend.domain.vote;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import project.votebackend.domain.BaseEntity;
import project.votebackend.domain.category.Category;
import project.votebackend.domain.comment.Comment;
import project.votebackend.domain.reaction.Reaction;
import project.votebackend.domain.user.User;
import project.votebackend.type.VoteStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "vote")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vote extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String title;
    private String content;
    private String link;
    private LocalDateTime finishTime;

    @Enumerated(EnumType.STRING)
    private VoteStatus status;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private Set<VoteOption> options = new HashSet<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private Set<VoteImage> images = new HashSet<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<VoteSelection> selections = new ArrayList<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<Reaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<Comment> comments = new ArrayList<>();
}
