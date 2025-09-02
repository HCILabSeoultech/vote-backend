package project.votebackend.repository.news;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.article.Cluster;
import project.votebackend.domain.reaction.NewsBookmark;
import project.votebackend.domain.user.User;

import java.util.Optional;

@Repository
public interface NewsBookmarkRepository extends JpaRepository<NewsBookmark, Long> {
    Optional<NewsBookmark> findByUserAndCluster(User user, Cluster cluster);
}
