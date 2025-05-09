package project.votebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.User;
import project.votebackend.domain.Vote;
import project.votebackend.domain.VoteSelection;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteSelectRepository extends JpaRepository<VoteSelection, Long> {

    Optional<VoteSelection> findByUserAndVote(User user, Vote vote);

    // 유저가 선택한 옵션 ID
    @Query(value = "SELECT option_id FROM vote_selections WHERE vote_id = :voteId AND user_id = :userId", nativeQuery = true)
    Optional<Long> findOptionIdByVoteIdAndUserId(@Param("voteId") Long voteId, @Param("userId") Long userId);

    // 해당 옵션에 대한 투표 수
    @Query(value = "SELECT COUNT(*) FROM vote_selections WHERE option_id = :optionId", nativeQuery = true)
    int countByOptionId(@Param("optionId") Long optionId);

    // 성별 기준 분석
    @Query("""
        SELECT u.gender, vo.option
        FROM VoteSelection vs
        JOIN vs.user u
        JOIN vs.option vo
        WHERE vs.vote.voteId = :voteId
    """)
    List<Object[]> findGenderStatistics(@Param("voteId") Long voteId);

    // 연령 기준 분석
    @Query("""
        SELECT u.birthdate, vo.option
        FROM VoteSelection vs
        JOIN vs.user u
        JOIN vs.option vo
        WHERE vs.vote.voteId = :voteId
    """)
    List<Object[]> findAgeStatistics(@Param("voteId") Long voteId);

    // 지역 기준 분석
    @Query("""
        SELECT u.address, vo.option
        FROM VoteSelection vs
        JOIN vs.user u
        JOIN vs.option vo
        WHERE vs.vote.voteId = :voteId
    """)
    List<Object[]> findRegionStatistics(@Param("voteId") Long voteId);
}
