package project.votebackend.domain;

import jakarta.persistence.*;
import lombok.*;

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
public class Vote extends BaseEntity{
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

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private Set<VoteOption> options = new HashSet<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private Set<VoteImage> images = new HashSet<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private List<VoteSelection> selections = new ArrayList<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private List<Reaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();
}
