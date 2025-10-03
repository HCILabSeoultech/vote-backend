package project.votebackend.dto.user;

import lombok.*;

import java.time.YearMonth;

@Getter
@AllArgsConstructor
public class MonthlyStat {
    private YearMonth month;
    private long receivedVotes;
}
