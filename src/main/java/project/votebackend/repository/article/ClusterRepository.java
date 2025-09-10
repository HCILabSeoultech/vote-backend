package project.votebackend.repository.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.article.Cluster;
import project.votebackend.type.Category;

import java.util.Optional;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, Long> {

    Optional<Cluster> findByTitle(String title);
    Page<Cluster> findByCategory(Category category, Pageable pageable);

    @Query(value = """
    SELECT 
      c.cluster_id,
      c.image_url,
      c.title,
      c.created_at
    FROM cluster c
    WHERE c.title ILIKE CONCAT('%', :keyword, '%')
    ORDER BY c.created_at DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM cluster c
    WHERE c.title ILIKE CONCAT('%', :keyword, '%')
    """,
            nativeQuery = true)
    Page<Object[]> searchClusterSummaries(@Param("keyword") String keyword, Pageable pageable);
}
