package project.votebackend.domain.vote;

import jakarta.persistence.*;
import lombok.*;
import project.votebackend.domain.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "vote_stat_6h")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteStat6h extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id")
    private Vote vote;

    private LocalDateTime statTime;         // 통계 시점 (6시간 단위 정각)

    private int totalVoteCount;             // 누적 득표 수
    private int todayVoteCount;             // 오늘 들어온 투표 수
    private int commentCount;               // 댓글 수

    private int rankTotal;                  // 누적 득표 기준 순위
    private int rankToday;                  // 오늘 득표 기준 순위
    private int rankComment;                // 댓글 수 기준 순위

    private int rankChangeTotal;            // 누적 득표 순위 변화
    private int rankChangeToday;            // 오늘 득표 순위 변화
    private int rankChangeComment;          // 댓글 순위 변화

    private int ongoingCommentRank;         // 진행중인 투표 댓글 수 랭킹
    private int ongoingVoteCountRank;       // 진행중인 투표 투표 수 랭킹

    private int ongoingCommentRankChange;   // 진행중인 투표 댓글 수 랭킹 변화
    private int ongoingVoteCountRankChange; // 진행중인 투표 투표 수 랭킹 변화

    private int endedCommentRank;         // 종료된 투표 댓글 수 랭킹
    private int endedVoteCountRank;       // 종료된 투표 투표 수 랭킹

    private int endedCommentRankChange;   // 종료된 투표 댓글 수 랭킹 변화
    private int endedVoteCountRankChange; // 종료된 투표 투표 수 랭킹 변화
}
