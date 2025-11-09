package project.votebackend.repository.article;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.article.QCluster;
import project.votebackend.dto.article.ClusterSummaryDto;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClusterQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<ClusterSummaryDto> searchClusterSummaries(String keyword, Pageable pageable) {
        QCluster cluster = QCluster.cluster;

        // 본문 쿼리
        List<ClusterSummaryDto> results = queryFactory
                .select(Projections.constructor(
                        ClusterSummaryDto.class,
                        cluster.id,
                        cluster.imageUrl,
                        cluster.title,
                        cluster.createdAt,
                        cluster.category
                ))
                .from(cluster)
                .where(cluster.title.containsIgnoreCase(keyword))
                .orderBy(cluster.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        Long total = queryFactory
                .select(cluster.count())
                .from(cluster)
                .where(cluster.title.containsIgnoreCase(keyword))
                .fetchOne();

        return new PageImpl<>(results, pageable, total == null ? 0 : total);
    }
}
