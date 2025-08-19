package project.votebackend.repository.article;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.article.Article;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a WHERE a.cluster.id = :clusterId AND a.url = :url")
    Optional<Article> findByClusterIdAndUrl(@Param("clusterId") Long clusterId, @Param("url") String url);
}
