package project.votebackend.scheduler;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.vote.Vote;
import project.votebackend.repository.vote.VoteRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiVoteDeleteScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final VoteRepository voteRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    public void deleteOldAiVotes() {
        LocalDateTime cutoff = LocalDateTime.now(KST).minusDays(14);
        int batchSize = 300; // 트래픽/메모리에 맞게 조절
        int total = 0;

        while (true) {
            var page = org.springframework.data.domain.PageRequest.of(0, batchSize);
            var ids = voteRepository.findAiVoteIdsBefore(cutoff, page);
            if (ids.isEmpty()) break;

            for (Long id : ids) {
                var ref = em.getReference(Vote.class, id);
                em.remove(ref);
            }
            em.flush();
            em.clear();
            total += ids.size();
        }

        log.info("[AiVoteCleanup] cutoff={} deletedVotes={}", cutoff, total);
    }
}
