package project.votebackend.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import project.votebackend.domain.BaseEntity;
import project.votebackend.domain.comment.Comment;
import project.votebackend.domain.comment.CommentLike;
import project.votebackend.domain.follow.Follow;
import project.votebackend.domain.reaction.NewsBookmark;
import project.votebackend.domain.reaction.Reaction;
import project.votebackend.domain.search.NewsSearch;
import project.votebackend.domain.vote.Vote;
import project.votebackend.domain.vote.VoteSelection;
import project.votebackend.type.Gender;
import project.votebackend.type.Grade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String phone;

    @Column(nullable = false)
    private String address;

    private String introduction;

    @Column(length = 1000)
    private String profileImage;

    @Column(nullable = false)
    private Long point;

    @Column(nullable = false)
    private Long voteScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Column(nullable = false)
    private LocalDate birthdate;

    @Column(nullable = false)
    private boolean draftHelpVersionSeen = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<UserInterest> userInterests = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @BatchSize(size = 50)
    private List<Vote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @BatchSize(size = 50)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @BatchSize(size = 50)
    private List<CommentLike> commentLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @BatchSize(size = 50)
    private List<Reaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.REMOVE)
    @BatchSize(size = 50)
    private List<Follow> followingList = new ArrayList<>();

    @OneToMany(mappedBy = "following", cascade = CascadeType.REMOVE)
    @BatchSize(size = 50)
    private List<Follow> followerList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @BatchSize(size = 50)
    private List<NewsBookmark> newsBookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @BatchSize(size = 50)
    private List<NewsSearch> newsSearches = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @BatchSize(size = 50)
    private List<VoteSelection> voteSelections = new ArrayList<>();
}
