package project.votebackend.domain.vote;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vote_stat_hourly")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteStatHourly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id")
    private Vote vote;

    private LocalDateTime statHour;     // 예: 2025-05-15 11:00 정각

    private int voteCount;              // 해당 1시간 동안 받은 투표 수
    private int rank;                   // 1시간 내 기준 랭킹
    private int rankChange;             // 이전 시간 대비 변화량

    private LocalDateTime createdAt;
}
