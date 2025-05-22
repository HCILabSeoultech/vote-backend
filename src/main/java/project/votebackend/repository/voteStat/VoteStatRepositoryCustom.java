package project.votebackend.repository.voteStat;

import java.time.LocalDateTime;
import java.util.List;

public interface VoteStatRepositoryCustom {
    List<Object[]> getDailyVoteStats(Long userId, LocalDateTime fromDate);
}
