package project.votebackend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.votebackend.service.article.ClusterIngestService;
import project.votebackend.type.Category;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClusterIngestScheduler {

    private final ClusterIngestService ingestService;

    @Value("${fastapi.base-url}")
    private String baseUrl;

    @Value("${fastapi.secure-prefix:/api/secure/tasks/get_latest}")
    private String securePrefix;

    // economy,it,life,politics,society,sports,world
    @Value("#{'${fastapi.categories}'.split(',')}")
    private List<String> categories;

    @Scheduled(cron = "0 0 */12 * * *", zone = "Asia/Seoul")
    public void runLatestIngest() {
        for (String categoryStr : categories) {
            final String url = String.format("%s%s/%s", baseUrl, securePrefix, categoryStr);
            try {
                Category category = Category.valueOf(categoryStr.toUpperCase()); // 문자열 → Enum 변환
                log.info("[INGEST] start: {} ({})", url, category);
                ingestService.ingestFromUrl(url, category);
                log.info("[INGEST] done : {} ({})", url, category);
            } catch (Exception e) {
                log.error("[INGEST] fail : {} - {}", url, e.getMessage(), e);
            }
        }
    }
}
