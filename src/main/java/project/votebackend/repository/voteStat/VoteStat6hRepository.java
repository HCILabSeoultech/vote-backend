package project.votebackend.repository.voteStat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.vote.VoteStat6h;

@Repository
public interface VoteStat6hRepository extends JpaRepository<VoteStat6h, Long> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM vote_stat_6h WHERE vote_id = :voteId", nativeQuery = true)
    void deleteByVoteId(@Param("voteId") Long voteId);
}
