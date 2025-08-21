package project.votebackend.service.article;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.votebackend.domain.article.Article;
import project.votebackend.domain.article.Cluster;
import project.votebackend.dto.article.ArticleItemDto;
import project.votebackend.dto.article.ClusterDetailDto;
import project.votebackend.dto.article.ClusterSummaryDto;
import project.votebackend.exception.ClusterException;
import project.votebackend.repository.article.ClusterRepository;
import project.votebackend.type.ErrorCode;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClusterService {

    private final ClusterRepository clusterRepository;

    // 뉴스 조회
    public Page<ClusterSummaryDto> getMainPageClusters(Pageable pageable) {
        return clusterRepository.findAll(pageable)
                .map(this::toSummary);
    }

    // 뉴스 상세 조회
    public ClusterDetailDto getClusterDetail(Long clusterId) {
        Cluster c = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ClusterException(ErrorCode.CLUSTER_NOT_FOUND));

        // 최신순 정렬
        List<ArticleItemDto> articles = c.getArticles().stream()
                .sorted(Comparator.comparing(Article::getPublishedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(a -> new ArticleItemDto(
                        a.getUrl(),
                        a.getOriginalUrl(),
                        a.getTitle(),
                        a.getPublisher(),
                        a.getPublishedAt()
                ))
                .toList();

        return new ClusterDetailDto(
                c.getId(),
                c.getImageUrl(),
                c.getTitle(),
                c.getCreatedAt(),
                c.getSubtitle1(),
                c.getSubtitle2(),
                c.getSubtitle3(),
                c.getSubtitle4(),
                c.getContent1(),
                c.getContent2(),
                c.getContent3(),
                c.getContent4(),
                articles
        );
    }

    private ClusterSummaryDto toSummary(Cluster c) {
        return new ClusterSummaryDto(
                c.getId(),
                c.getImageUrl(),
                c.getTitle(),
                c.getCreatedAt()
        );
    }
}
