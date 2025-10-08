package project.votebackend.scheduler;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.article.Cluster;
import project.votebackend.repository.article.ClusterRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClusterDeleteScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final ClusterRepository clusterRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void deleteOldClusters() {
        LocalDateTime cutoff = LocalDateTime.now(KST).minusDays(7);
        int batchSize = 300;
        int total = 0;

        while (true) {
            List<Long> ids = clusterRepository.findIdsByCreatedAtBefore(
                    cutoff, org.springframework.data.domain.PageRequest.of(0, batchSize));
            if (ids.isEmpty()) break;

            for (Long id : ids) {
                Cluster ref = em.getReference(Cluster.class, id);
                em.remove(ref);
            }
            em.flush();
            em.clear();
            total += ids.size();
        }

        log.info("[ClusterCleanup] cutoff={} deletedClusters={}", cutoff, total);
    }
}
