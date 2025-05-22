package project.votebackend.repository.voteStat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.vote.VoteStatHourly;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteStatHourlyRepository extends JpaRepository<VoteStatHourly, Long> {

    // 가장 최근 statHour (정각) 기준 시간 조회
    @Query("SELECT MAX(vs.statHour) FROM VoteStatHourly vs")
    LocalDateTime findLatestStatHour();

    // 최근 1시간 동안 진행중인 투표의 통계 페이징 조회
    Page<VoteStatHourly> findByStatHourAndVote_FinishTimeAfterOrderByVoteCountDesc(LocalDateTime statHour, LocalDateTime now, Pageable pageable);

    // 이전 1시간 단위 시간 조회
    @Query("SELECT MAX(v.statHour) FROM VoteStatHourly v WHERE v.statHour < :now")
    Optional<LocalDateTime> findLatestStatTimeBefore(@Param("now") LocalDateTime now);

    // 해당 시간의 모든 통계 조회
    List<VoteStatHourly> findByStatHour(LocalDateTime statHour);

    // 해당 시간 통계 삭제
    void deleteByStatHour(LocalDateTime statHour);
}