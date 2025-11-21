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
