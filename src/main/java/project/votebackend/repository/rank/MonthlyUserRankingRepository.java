package project.votebackend.repository.rank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.rank.MonthlyUserRanking;

@Repository
public interface MonthlyUserRankingRepository extends JpaRepository<MonthlyUserRanking, Long> {
}
