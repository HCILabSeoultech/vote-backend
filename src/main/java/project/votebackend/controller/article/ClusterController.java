package project.votebackend.controller.article;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.article.ClusterSummaryDto;
import project.votebackend.service.article.ClusterService;
import project.votebackend.util.PageResponseUtil;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cluster")
public class ClusterController {

    private final ClusterService clusterService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> loadMainPageClusters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ClusterSummaryDto> clusterPage = clusterService.getMainPageClusters(pageable);
        return ResponseEntity.ok(PageResponseUtil.toResponse(clusterPage));
    }
}
