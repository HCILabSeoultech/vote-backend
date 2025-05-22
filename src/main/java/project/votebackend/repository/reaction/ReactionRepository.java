package project.votebackend.repository.reaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.reaction.Reaction;
import project.votebackend.domain.user.User;
import project.votebackend.domain.vote.Vote;
import project.votebackend.type.ReactionType;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByUserAndVoteAndReaction(User user, Vote vote, ReactionType reaction);

    //리액션 수 카운트
    @Query("""
        SELECT r.vote.voteId, r.reaction, r.user.userId
        FROM Reaction r
        WHERE r.vote.voteId IN :voteIds
    """)
    List<Object[]> findReactionsByVoteIds(@Param("voteIds") List<Long> voteIds);
}
