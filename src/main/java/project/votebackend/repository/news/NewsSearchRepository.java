package project.votebackend.repository.news;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.search.NewsSearch;

@Repository
public interface NewsSearchRepository extends JpaRepository<NewsSearch, Long> {
}
