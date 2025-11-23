package project.votebackend.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.votebackend.service.rank.RankRedisService;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class UserRankScheduler {

    private final RankRedisService rankRedisService;

    // 매월 1일 00:00:30 에 실행
    @Scheduled(cron = "5 0 0 1 * *")
    public void settleLastMonth() {

        LocalDate lastMonth = LocalDate.now().minusMonths(1);

        int year = lastMonth.getYear();
        int month = lastMonth.getMonthValue();

        rankRedisService.settleMonthlyRanking(year, month);
    }
}
