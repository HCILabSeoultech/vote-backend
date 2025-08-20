package project.votebackend.controller.article;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.votebackend.dto.article.IngestPayload;
import project.votebackend.service.article.ClusterIngestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ingest")
public class ClusterIngestController {

    private final ClusterIngestService ingestService;

    @GetMapping
    @Operation(summary = "뉴스 생성 API", description = "AI와 연동하여 생성된 뉴스 기사를 받아옵니다.")
    public ResponseEntity<String> ingestFromExternal(@RequestParam("source") String sourceUrl) {
        ingestService.ingestFromUrl(sourceUrl);
        return ResponseEntity.ok("OK");
    }

    @PostMapping
    @Operation(summary = "JSON 파싱 테스트 API", description = "JSON 파일의 파싱 과정을 검증하기 위한 API입니다.")
    public ResponseEntity<String> ingestFromBody(@RequestBody IngestPayload payload) {
        ingestService.ingest(payload);
        return ResponseEntity.ok("OK");
    }
}
