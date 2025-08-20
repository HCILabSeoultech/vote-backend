package project.votebackend.service.article;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.votebackend.domain.article.Cluster;
import project.votebackend.dto.article.ClusterSummaryDto;
import project.votebackend.repository.article.ClusterRepository;

@Service
@RequiredArgsConstructor
public class ClusterService {

    private final ClusterRepository clusterRepository;

    public Page<ClusterSummaryDto> getMainPageClusters(Pageable pageable) {
        return clusterRepository.findAll(pageable)
                .map(this::toSummary);
    }

    private ClusterSummaryDto toSummary(Cluster c) {
        return new ClusterSummaryDto(
                c.getId(),
                c.getTitle(),
                c.getImageUrl(),
                c.getCreatedAt()
        );
    }
}
