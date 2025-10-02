package project.votebackend.repository.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Repository
@Transactional(readOnly = true)
public class UserStatDao {

    @PersistenceContext
    private EntityManager em;

    // 이번 달 포함 최근 6개월 (월을 'YYYY-MM' 문자열로 반환)
    public List<Object[]> findMonthlyReceivedVotes6(Long userId) {
        String sql = """
            WITH bounds AS (
              SELECT date_trunc('month', (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul'))::date AS this_month_start_kst
            ),
            months AS (
              SELECT generate_series(
                       (SELECT this_month_start_kst FROM bounds) - interval '5 months',
                       (SELECT this_month_start_kst FROM bounds),
                       interval '1 month'
                     )::date AS month_start
            )
            SELECT 
              TO_CHAR(m.month_start, 'YYYY-MM') AS month_str,
              COALESCE(x.cnt, 0)               AS received_votes
            FROM months m
            LEFT JOIN (
              SELECT
                TO_CHAR(date_trunc('month', (vs.created_at AT TIME ZONE 'Asia/Seoul'))::date, 'YYYY-MM') AS month_str,
                COUNT(*)::bigint AS cnt
              FROM vote_selections vs
              JOIN vote v ON v.vote_id = vs.vote_id
              WHERE v.user_id = :userId
                AND (vs.created_at AT TIME ZONE 'Asia/Seoul') >= ((SELECT this_month_start_kst FROM bounds) - interval '5 months')
                AND (vs.created_at AT TIME ZONE 'Asia/Seoul') <  ((SELECT this_month_start_kst FROM bounds) + interval '1 month')
              GROUP BY 1
            ) x ON x.month_str = TO_CHAR(m.month_start, 'YYYY-MM')
            ORDER BY m.month_start
            """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("userId", userId);
        return q.getResultList(); // Object[] { String month_str, BigInteger cnt }
    }

    // 지금까지 받은 투표수
    public long findTotalReceivedVotes(Long userId) {
        String sql = """
            WITH u AS (
              SELECT date_trunc('month', (u.created_at AT TIME ZONE 'Asia/Seoul'))::date AS signup_month_kst
              FROM users u WHERE u.user_id = :userId
            ),
            now_m AS (
              SELECT date_trunc('month', (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul'))::date AS this_month_kst
            )
            SELECT COUNT(*) 
            FROM vote_selections vs
            JOIN vote v ON v.vote_id = vs.vote_id
            WHERE v.user_id = :userId
              AND (vs.created_at AT TIME ZONE 'Asia/Seoul') >= (SELECT signup_month_kst FROM u)
              AND (vs.created_at AT TIME ZONE 'Asia/Seoul') <  ((SELECT this_month_kst FROM now_m) + interval '1 month')
            """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("userId", userId);
        Number n = (Number) q.getSingleResult();
        return n.longValue();
    }

    // 가입 후 지금까지의 월 수
    public int findMonthsSinceSignup(Long userId) {
        String sql = """
            SELECT (
              EXTRACT(YEAR FROM age(date_trunc('month', (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')),
                                    date_trunc('month', (u.created_at AT TIME ZONE 'Asia/Seoul')))) * 12
              + EXTRACT(MONTH FROM age(date_trunc('month', (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')),
                                       date_trunc('month', (u.created_at AT TIME ZONE 'Asia/Seoul'))))
              + 1
            )::int
            FROM users u WHERE u.user_id = :userId
            """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("userId", userId);
        Number n = (Number) q.getSingleResult();
        return n.intValue();
    }
}
