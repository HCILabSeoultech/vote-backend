package project.votebackend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.votebackend.service.VoteSchedulingService;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoteStatScheduler {
    private final VoteSchedulingService voteSchedulingService;

    // 6시간 단위 통계 갱신
    @Scheduled(cron = "0 0 */6 * * *") // 매일 0시, 6시, 12시, 18시
    public void run6hVoteStatUpdate() {
        log.info("[스케줄] 6시간 단위 통계 시작");
        voteSchedulingService.generate6hStats();
    }

    // 1시간 단위 관심 급등 통계 갱신
    @Scheduled(cron = "0 2 * * * *") // 매 정각
    public void runHourlyTrendingStatUpdate() {
        log.info("[스케줄] 1시간 단위 관심 급등 통계 시작");
        voteSchedulingService.generateHourlyStats();
    }
}
