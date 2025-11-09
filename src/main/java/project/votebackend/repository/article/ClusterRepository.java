package project.votebackend.repository.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.article.Cluster;
import project.votebackend.type.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, Long> {

    Optional<Cluster> findByTitle(String title);
    Page<Cluster> findByCategory(Category category, Pageable pageable);

    @Query("""
       select c.id from Cluster c
       where c.createdAt < :cutoff
       order by c.id asc
    """)
    List<Long> findIdsByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);
}
