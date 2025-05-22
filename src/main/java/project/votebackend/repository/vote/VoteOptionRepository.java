package project.votebackend.repository.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.vote.VoteOption;

@Repository
public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
    void deleteByVote_VoteId(Long voteId);
}
