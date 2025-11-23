package project.votebackend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserMonthlyRankDto {
    Long userId;
    String username;
    Long totalVotes;
}
