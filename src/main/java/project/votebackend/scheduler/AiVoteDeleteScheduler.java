package project.votebackend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.repository.vote.VoteRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiVoteDeleteScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final VoteRepository voteRepository;


    @Transactional
    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    public void deleteOldAiVotes() {
        LocalDateTime cutoff = LocalDateTime.now(KST).minusDays(8);
        int deleted = voteRepository.deleteAiVotesOlderThan(cutoff);
        log.info("[AiVoteCleanup] deleted={} cutoff={}", deleted, cutoff);
    }
}
