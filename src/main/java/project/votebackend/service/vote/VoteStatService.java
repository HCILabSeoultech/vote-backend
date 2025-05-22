package project.votebackend.service.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.votebackend.dto.vote.DailyVoteStatDto;
import project.votebackend.repository.voteStat.VoteStatRepositoryCustom;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteStatService {

    private final VoteStatRepositoryCustom voteStatRepository;

    //최근 일주일 투표 생성, 참여 스탯 조회
    public List<DailyVoteStatDto> getLast7DaysStats(Long userId) {
        LocalDateTime fromDate = LocalDate.now().minusDays(6).atStartOfDay(); // "YYYY-MM-DD"

        List<Object[]> rawStats = voteStatRepository.getDailyVoteStats(userId, fromDate);

        return rawStats.stream()
                .map(row -> new DailyVoteStatDto(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }
}
