package project.votebackend.repository.vote;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.vote.Vote;
import project.votebackend.type.ReactionType;
import project.votebackend.type.VoteStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    //사용자가 작성한 글 조회
    Page<Vote> findByUser_UserIdAndStatus(Long userId, VoteStatus voteState, Pageable pageable);

    //사용자의 게시글 개수
    Long countByUser_UserId(Long userId);

    //작성한 글 + 내가 선택글 관심사 + 팔로우한 사람의 글
    @Query(value = """
    WITH base AS (
      SELECT v.*,
             (v.finish_time IS NULL OR v.finish_time > NOW()) AS is_open,
             EXISTS (
               SELECT 1 FROM vote_selections s
               WHERE s.vote_id = v.vote_id
                 AND s.user_id = :userId
             ) AS participated
      FROM vote v
      WHERE v.status = 'PUBLISHED'
        AND (v.finish_time IS NULL OR v.finish_time > NOW())
        AND v.user_id <> :aiUserId
    ),
    base_scored AS (
      SELECT b.*,
             CASE
               WHEN b.is_open AND NOT b.participated THEN 2  -- 미참여 진행중
               WHEN b.is_open AND b.participated     THEN 1  -- 참여 진행중
               ELSE 0
             END AS pr
      FROM base b
    )
    SELECT *
    FROM (
         -- 내가 작성
         SELECT * FROM base_scored WHERE user_id = :userId
         UNION
         -- 관심 카테고리
         SELECT * FROM base_scored WHERE category_id IN (:categoryIds)
         UNION
         -- 팔로우
         SELECT bs.* FROM base_scored bs
          WHERE bs.user_id IN (
            SELECT f.following_id
            FROM follow f
            WHERE f.follower_id = :userId
          )
    ) t
    ORDER BY t.pr DESC, t.created_at DESC, t.vote_id DESC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Vote> findMainPageVotesUnion(
            @Param("userId") Long userId,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("aiUserId") Long aiUserId
    );

    // AI 후보
    @Query(value = """
        WITH ai_pool AS (
          SELECT v.*
          FROM vote v
          WHERE v.status='PUBLISHED'
            AND (v.finish_time IS NULL OR v.finish_time > NOW())
            AND v.category_id IN (:categoryIds)
            AND v.user_id = :aiUserId
            AND NOT EXISTS (
               SELECT 1
               FROM vote_selections s
               WHERE s.vote_id = v.vote_id
               AND s.user_id = :userId
            )
        )
        SELECT * FROM ai_pool v
        ORDER BY created_at DESC, vote_id DESC
        LIMIT :limit
      """, nativeQuery = true)
    List<Vote> findAiCandidatesForCategories(
            @Param("userId") Long userId,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("limit") int limit,
            @Param("aiUserId") Long aiUserId
    );

    // 인기 후보
    @Query("""
      SELECT v
      FROM Vote v
      WHERE v.status = 'PUBLISHED'
        AND (v.finishTime IS NULL OR v.finishTime > CURRENT_TIMESTAMP)
      ORDER BY
        CASE
          WHEN NOT EXISTS (
            SELECT 1 FROM VoteSelection vs
            WHERE vs.vote = v AND vs.user.userId = :userId
          ) THEN 2
          WHEN EXISTS (
            SELECT 1 FROM VoteSelection vs2
            WHERE vs2.vote = v AND vs2.user.userId = :userId
          ) THEN 1
          ELSE 0
        END DESC,
        (SELECT COUNT(s2) FROM VoteSelection s2 WHERE s2.vote = v) DESC,
        v.createdAt DESC, v.voteId DESC
    """)
    List<Vote> findPopularCandidatesGlobal(
            @Param("userId") Long userId,
            Pageable pageable
    );

    //내가 투표한 글
    @EntityGraph(attributePaths = {
            "category", "user"
    })
    @Query("""
        SELECT DISTINCT v FROM Vote v
        JOIN v.selections s
        WHERE s.user.userId = :userId AND v.status = 'PUBLISHED'
        ORDER BY v.createdAt DESC
    """)
    Page<Vote> findVotedByUserId(@Param("userId") Long userId, Pageable pageable);

    //내가 북마크한 글
    @EntityGraph(attributePaths = {
            "category", "user"
    })
    @Query("""
        SELECT DISTINCT v FROM Vote v
        JOIN v.reactions r
        WHERE r.user.userId = :userId AND r.reaction = 'BOOKMARK' AND v.status = 'PUBLISHED'
        ORDER BY v.createdAt DESC
    """)
    Page<Vote> findBookmarkedVotes(@Param("userId") Long userId, Pageable pageable);

    //단일 글
    @Query("SELECT v FROM Vote v " +
            "JOIN FETCH v.user " +
            "LEFT JOIN FETCH v.options o " +
            "LEFT JOIN FETCH v.reactions r " +
            "LEFT JOIN FETCH v.images i " +
            "WHERE v.voteId = :voteId")
    Optional<Vote> findByIdWithUserAndOptions(@Param("voteId") Long voteId);

    //특정 카테고리의 글 조회
    @Query("""
        SELECT v
        FROM Vote v
        LEFT JOIN v.reactions r ON r.reaction = 'LIKE'
        WHERE v.category.categoryId = :categoryId
        GROUP BY v
        ORDER BY COUNT(r) DESC
    """)
    Page<Vote> findByCategoryOrderByLikeCount(@Param("categoryId") Long categoryId, Pageable pageable);

    //총투표수 기준 정렬
    @Query("""
        SELECT v
        FROM Vote v
        LEFT JOIN v.selections s
        WHERE
            (:status = 'ALL')
            OR (:status = 'ONGOING' AND v.finishTime > CURRENT_TIMESTAMP)
            OR (:status = 'ENDED' AND v.finishTime <= CURRENT_TIMESTAMP)
        GROUP BY v
        ORDER BY COUNT(s) DESC
    """)
    List<Vote> findVotesSortedByTotalVotes(@Param("status") String status, Pageable pageable);

    // 댓글 수 기준 정렬
    @Query("""
        SELECT v FROM Vote v
        LEFT JOIN v.comments c
        WHERE 
            (:status = 'ALL')
            OR (:status = 'ONGOING' AND v.finishTime > CURRENT_TIMESTAMP)
            OR (:status = 'ENDED' AND v.finishTime <= CURRENT_TIMESTAMP)
        GROUP BY v
        ORDER BY COUNT(c) DESC
    """)
    List<Vote> findVotesSortedByComments(@Param("status") String status, Pageable pageable);

    // 좋아요 수 기준 정렬 (Reaction에서 LIKE만 세기)
    @Query("""
        SELECT v FROM Vote v
        LEFT JOIN v.reactions r WITH r.reaction = 'LIKE'
        WHERE 
            (:status = 'ALL')
            OR (:status = 'ONGOING' AND v.finishTime > CURRENT_TIMESTAMP)
            OR (:status = 'ENDED' AND v.finishTime <= CURRENT_TIMESTAMP)
        GROUP BY v
        ORDER BY COUNT(r) DESC
    """)
    List<Vote> findVotesSortedByLikes(@Param("status") String status, Pageable pageable);

    // 특정 유저가 이번 달에 생성한 투표 조회
    List<Vote> findByUser_UserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 투표 검색
    @Query(value = """
    SELECT 
        v.vote_id,
        v.title,
        COALESCE(s.selection_count, 0) AS participant_count,
        COALESCE(r.like_count, 0) AS like_count,
        COALESCE(c.comment_count, 0) AS comment_count
    FROM vote v
    LEFT JOIN (
        SELECT vote_id, COUNT(*) AS selection_count
        FROM vote_selections
        GROUP BY vote_id
    ) s ON v.vote_id = s.vote_id
    LEFT JOIN (
        SELECT vote_id, COUNT(*) AS like_count
        FROM reaction
        WHERE reaction = 'LIKE'
        GROUP BY vote_id
    ) r ON v.vote_id = r.vote_id
    LEFT JOIN (
        SELECT vote_id, COUNT(*) AS comment_count
        FROM comment
        WHERE parent_id IS NULL
        GROUP BY vote_id
    ) c ON v.vote_id = c.vote_id
    WHERE v.title ILIKE CONCAT('%', :keyword, '%')
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM vote v
    WHERE v.title ILIKE CONCAT('%', :keyword, '%')
    """,
            nativeQuery = true)
    Page<Object[]> searchVotesWithStats(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
    SELECT 
        v.*, 
        COALESCE(c.comment_count, 0) AS comment_count,
        COALESCE(r.like_count, 0) AS like_count,
        COALESCE(s.vote_count, 0) AS vote_count,
        (
            COALESCE(c.comment_count, 0) * 1.5 +
            COALESCE(r.like_count, 0) * 1.2 +
            COALESCE(s.vote_count, 0) * 1.0
        ) AS popularity_score
    FROM vote v
    LEFT JOIN (
        SELECT vote_id, COUNT(*) AS comment_count
        FROM comment
        WHERE parent_id IS NULL
        GROUP BY vote_id
    ) c ON v.vote_id = c.vote_id
    LEFT JOIN (
        SELECT vote_id, COUNT(*) AS like_count
        FROM reaction
        WHERE reaction = 'LIKE'
        GROUP BY vote_id
    ) r ON v.vote_id = r.vote_id
    LEFT JOIN (
        SELECT vote_id, COUNT(*) AS vote_count
        FROM vote_selections
        GROUP BY vote_id
    ) s ON v.vote_id = s.vote_id
    WHERE 
        (:status = 'ALL')
        OR (:status = 'ONGOING' AND v.finish_time > CURRENT_TIMESTAMP)
        OR (:status = 'ENDED' AND v.finish_time <= CURRENT_TIMESTAMP)
    ORDER BY popularity_score DESC
    LIMIT :limit OFFSET :offset
    """,
            nativeQuery = true)
    List<Vote> findVotesByTrending(
            @Param("status") String status,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    boolean existsByVoteIdAndUser_UserId(Long voteId, Long userId);

    @Query("""
        select v.voteId
        from Vote v
        where v.createdByAI = true
          and v.createdAt < :cutoff
        order by v.voteId asc
    """)
    List<Long> findAiVoteIdsBefore(@Param("cutoff") LocalDateTime cutoff,
                                   org.springframework.data.domain.Pageable pageable);
}
