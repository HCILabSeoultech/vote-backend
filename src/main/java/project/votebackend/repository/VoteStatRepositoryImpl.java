package project.votebackend.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class VoteStatRepositoryImpl implements VoteStatRepositoryCustom{

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Object[]> getDailyVoteStats(Long userId, LocalDateTime fromDate) {
        String sql = """
            SELECT
              d.date AS date,
              COALESCE(vc.created_count, 0) AS createdCount,
              COALESCE(vs.selected_count, 0) AS selectedCount
            FROM (
              SELECT to_char(current_date - offs, 'YYYY-MM-DD') AS date
              FROM generate_series(0, 6) AS offs
            ) d
            LEFT JOIN (
              SELECT to_char(created_at, 'YYYY-MM-DD') AS date, COUNT(*) AS created_count
              FROM vote
              WHERE user_id = :userId AND created_at >= :fromDate
              GROUP BY to_char(created_at, 'YYYY-MM-DD')
            ) vc ON d.date = vc.date
            LEFT JOIN (
              SELECT to_char(created_at, 'YYYY-MM-DD') AS date, COUNT(*) AS selected_count
              FROM vote_selections
              WHERE user_id = :userId AND created_at >= :fromDate
              GROUP BY to_char(created_at, 'YYYY-MM-DD')
            ) vs ON d.date = vs.date
            ORDER BY d.date
        """;

        return em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("fromDate", fromDate)
                .getResultList();
    }
}
