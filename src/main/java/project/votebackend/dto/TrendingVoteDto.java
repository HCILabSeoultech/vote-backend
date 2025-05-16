package project.votebackend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrendingVoteDto {
    private Long voteId;
    private String title;
    private String thumbnailImageUrl;

    private int hourlyVoteCount;         // 최근 1시간 득표 수
    private int hourlyRank;              // 관심급등 랭킹
    private int hourlyRankChange;        // 순위 변화량

    private int totalVotes;              // 누적 투표 수 (참고용)
    private int todayVotes;              // 오늘 투표 수 (참고용)
    private int commentCount;
    private LocalDateTime finishTime;
}
