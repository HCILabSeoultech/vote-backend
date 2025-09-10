package project.votebackend.controller.article;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.votebackend.dto.article.ClusterDetailDto;
import project.votebackend.dto.article.ClusterSummaryDto;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.article.ClusterService;
import project.votebackend.type.Category;
import project.votebackend.util.PageResponseUtil;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cluster")
public class ClusterController {

    private final ClusterService clusterService;

    // 뉴스 목록 조회
    @GetMapping
    @Operation(summary = "뉴스 목록 조회 API", description = "생성된 뉴스들을 조회합니다. category 파라미터로 카테고리별 조회를 지원합니다.")
    public ResponseEntity<Map<String, Object>> loadNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Category category
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ClusterSummaryDto> clusterPage = clusterService.getClusters(pageable, category);
        return ResponseEntity.ok(PageResponseUtil.toResponse(clusterPage));
    }

    // 상세 조회
    @GetMapping("/{clusterId}")
    @Operation(summary = "뉴스 상세 조회 API", description = "뉴스를 상세조회 합니다.")
    public ResponseEntity<ClusterDetailDto> getClusterDetail(
            @PathVariable Long clusterId,
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        return ResponseEntity.ok(clusterService.getClusterDetail(clusterId, userDetails.getId()));
    }
}
