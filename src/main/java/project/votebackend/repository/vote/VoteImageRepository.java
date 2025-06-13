package project.votebackend.repository.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.vote.VoteImage;

@Repository
public interface VoteImageRepository extends JpaRepository<VoteImage, Long> {
    void deleteByVote_VoteId(Long voteId);
}
