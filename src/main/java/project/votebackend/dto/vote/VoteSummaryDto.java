package project.votebackend.dto.vote;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteSummaryDto {
    private Long voteId;
    private String title;
    private String thumbnailImageUrl;

    private int totalVotes;              // 누적 투표 수
    private int todayVotes;              // 오늘 투표 수
    private int commentCount;            // 댓글 수
    private LocalDateTime finishTime;

    private int rankTotal;               // 누적 투표 기준 랭킹
    private int rankToday;               // 오늘 득표 기준 랭킹
    private int rankComment;             // 댓글 기준 랭킹

    private int rankChangeTotal;         // 누적 득표 순위 변화
    private int rankChangeToday;         // 오늘 득표 순위 변화
    private int rankChangeComment;
}
