package project.votebackend.repository.news;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.search.NewsSearch;

@Repository
public interface NewsSearchRepository extends JpaRepository<NewsSearch, Long> {
    void deleteBySearchIdAndUser_UserId(Long searchId, Long userId);
    void deleteByUser_UserId(Long userId);
}
