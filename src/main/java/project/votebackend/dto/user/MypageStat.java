package project.votebackend.dto.user;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class MypageStat {
    private List<MonthlyStat> monthly; // 최근 5개월 + 이번달 (총 6개)
    private long total;                // 가입 이후 총 받은 투표 수
    private int months;                // 가입 월 ~ 이번 달 개월 수
}
