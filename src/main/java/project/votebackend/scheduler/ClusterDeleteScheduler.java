package project.votebackend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.repository.article.ClusterRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClusterDeleteScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final ClusterRepository clusterRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void deleteOldClusters() {
        LocalDateTime cutoff = LocalDateTime.now(KST).minusDays(8);
        int deleted = clusterRepository.deleteOlderThan(cutoff);
        log.info("[ClusterCleanup] deleted={} cutoff={}", deleted, cutoff);
    }
}
