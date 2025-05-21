package project.votebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyVoteStatDto {
    private String date;           // yyyy-MM-dd
    private long createdCount;     // 생성한 투표 수
    private long selectedCount;    // 참여한 투표 수
}
