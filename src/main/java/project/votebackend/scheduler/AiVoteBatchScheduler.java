package project.votebackend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.votebackend.service.vote.AiVoteBatchIngestService;
import project.votebackend.type.Category;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiVoteBatchScheduler {

    private final AiVoteBatchIngestService ingestService;

    @Value("${fastapi.base-url}")
    private String baseUrl;

    @Value("${fastapi.secure-vote-prefix:/api/secure/tasks/get_aivote}")
    private String secureVotePrefix;

    // economy,it,life,politics,society,sports,world
    @Value("#{'${fastapi.categories}'.split(',')}")
    private List<String> categories;

    @Scheduled(cron = "0 30 */12 * * *", zone = "Asia/Seoul")
    public void runVoteBatchIngest() {
        for (String categoryStr : categories) {
            final String url = String.format("%s%s/%s", baseUrl, secureVotePrefix, categoryStr);
            try {
                Category category = Category.valueOf(categoryStr.toUpperCase());
                log.info("[AI-VOTE] ingest start: {} ({})", url, category);
                ingestService.ingestVotesFromUrl(url);
                log.info("[AI-VOTE] ingest done : {} ({})", url, category);
            } catch (Exception e) {
                log.error("[AI-VOTE] ingest fail : {} - {}", url, e.getMessage(), e);
            }
        }
    }
}
