package project.votebackend.service.rank;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.votebackend.dto.user.UserMonthlyRankDto;
import project.votebackend.repository.rank.RankQueryRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {

    private final RankQueryRepository rankQueryRepository;

    public List<UserMonthlyRankDto> getMonthlyRank(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        return rankQueryRepository.getMonthlyRanking(date);
    }
}
