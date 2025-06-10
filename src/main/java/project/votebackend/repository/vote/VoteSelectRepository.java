package project.votebackend.repository.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.user.User;
import project.votebackend.domain.vote.Vote;
import project.votebackend.domain.vote.VoteSelection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // 유저가 참여한 투표 수
    @Query("SELECT COUNT(vs) FROM VoteSelection vs WHERE vs.user.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    // 옵션 수 카운트
    @Query(value = """
        SELECT vs.option_id, COUNT(*) AS vote_count
        FROM vote_selections vs
        WHERE vs.vote_id IN :voteIds
        GROUP BY vs.option_id
    """, nativeQuery = true)
    List<Object[]> findOptionVoteCounts(@Param("voteIds") List<Long> voteIds);

    // 성별 기준 분석 최적화
    @Query("""
        SELECT u.gender, vo.option, COUNT(vs)
        FROM VoteSelection vs
        JOIN vs.user u
        JOIN vs.option vo
        WHERE vs.vote.voteId = :voteId
        GROUP BY u.gender, vo.option
    """)
    List<Object[]> findGenderStatistics(@Param("voteId") Long voteId);

    // 연령 기준 분석 최적화
    @Query(value = """
        SELECT 
            FLOOR(EXTRACT(YEAR FROM AGE(CURRENT_DATE, u.birthdate)) / 10) * 10 AS age_group,
            o.option AS option,
            COUNT(*) AS count
        FROM vote_selections vs
        JOIN users u ON vs.user_id = u.user_id
        JOIN vote_option o ON vs.option_id = o.option_id
        WHERE vs.vote_id = :voteId
        GROUP BY age_group, o.option
    """, nativeQuery = true)
    List<Object[]> findAgeStatistics(@Param("voteId") Long voteId);

    // 지역 기준 분석 최적화
    @Query("""
        SELECT u.address, vo.option, COUNT(vs)
        FROM VoteSelection vs
        JOIN vs.user u
        JOIN vs.option vo
        WHERE vs.vote.voteId = :voteId
        GROUP BY u.address, vo.option
    """)
    List<Object[]> findRegionStatistics(@Param("voteId") Long voteId);

    void deleteByVote_VoteId(Long voteId);

    // 평균 투표 수 계산
    @Query("SELECT vs.vote.voteId, COUNT(vs) FROM VoteSelection vs WHERE vs.vote.voteId IN :voteIds GROUP BY vs.vote.voteId")
    List<Object[]> countRawByVoteIdsGrouped(@Param("voteIds") List<Long> voteIds);

    default Map<Long, Long> countByVoteIdsGroupedIncludingZero(List<Long> voteIds) {
        List<Object[]> raw = countRawByVoteIdsGrouped(voteIds);
        Map<Long, Long> resultMap = new HashMap<>();

        // 0으로 초기화
        for (Long voteId : voteIds) {
            resultMap.put(voteId, 0L);
        }

        // 참여자 수가 있는 경우 덮어쓰기
        for (Object[] row : raw) {
            resultMap.put((Long) row[0], (Long) row[1]);
        }

        return resultMap;
    }
}
