package project.votebackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.VoteStat6h;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteStat6hRepository extends JpaRepository<VoteStat6h, Long> {

    // 가장 최신 통계 시점 조회 (정각 단위)
    @Query("SELECT MAX(vs.statTime) FROM VoteStat6h vs")
    LocalDateTime findLatestStatTime();

    // 진행중인 투표 전체 득표수 기준 정렬
    Page<VoteStat6h> findByStatTimeAndVote_FinishTimeAfterOrderByTotalVoteCountDesc(LocalDateTime statTime, LocalDateTime now, Pageable pageable);

    // 진행중인 투표 오늘 득표수 기준 정렬
    Page<VoteStat6h> findByStatTimeAndVote_FinishTimeAfterOrderByTodayVoteCountDesc(LocalDateTime statTime, LocalDateTime now, Pageable pageable);

    // 진행중인 투표 댓글수 기준 정렬
    Page<VoteStat6h> findByStatTimeAndVote_FinishTimeAfterOrderByCommentCountDesc(LocalDateTime statTime, LocalDateTime now, Pageable pageable);

    // 진행중인 투표 전체 득표수 기준 정렬
    Page<VoteStat6h> findByStatTimeAndVote_FinishTimeLessThanEqualOrderByTotalVoteCountDesc(LocalDateTime statTime, LocalDateTime now, Pageable pageable);

    // 진행중인 투표 댓글수 기준 정렬
    Page<VoteStat6h> findByStatTimeAndVote_FinishTimeLessThanEqualOrderByCommentCountDesc(LocalDateTime statTime, LocalDateTime now, Pageable pageable);

    // 이전 1시간 단위 시간 조회
    @Query("SELECT MAX(v.statTime) FROM VoteStat6h v WHERE v.statTime < :now")
    Optional<LocalDateTime> findLatestStatTimeBefore(@Param("now") LocalDateTime now);

    //해당 시간의 모든 통계 조회
    List<VoteStat6h> findByStatTime(LocalDateTime statTime);

    //해당 시간 통계 삭제
    void deleteByStatTime(LocalDateTime statTime);
}
