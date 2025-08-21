package project.votebackend.controller.article;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.votebackend.dto.article.ClusterDetailDto;
import project.votebackend.dto.article.ClusterSummaryDto;
import project.votebackend.service.article.ClusterService;
import project.votebackend.util.PageResponseUtil;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cluster")
public class ClusterController {

    private final ClusterService clusterService;

    // 뉴스 목록 조회
    @GetMapping
    @Operation(summary = "뉴스 목록 조회 API", description = "생성된 뉴스들을 조회합니다.")
    public ResponseEntity<Map<String, Object>> loadMainPageClusters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ClusterSummaryDto> clusterPage = clusterService.getMainPageClusters(pageable);
        return ResponseEntity.ok(PageResponseUtil.toResponse(clusterPage));
    }

    // 상세 조회
    @GetMapping("/{clusterId}")
    @Operation(summary = "뉴스 상세 조회 API", description = "뉴스를 상세조회 합니다.")
    public ResponseEntity<ClusterDetailDto> getClusterDetail(@PathVariable Long clusterId) {
        return ResponseEntity.ok(clusterService.getClusterDetail(clusterId));
    }

}
