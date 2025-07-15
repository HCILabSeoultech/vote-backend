package project.votebackend.repository.vote;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.vote.Vote;
import project.votebackend.type.ReactionType;
import project.votebackend.type.VoteStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    //내가 작성한 글
    Page<Vote> findByUser_UserId(Long userId, Pageable pageable);

    //다른 사용자가 작성한
    Page<Vote> findByUser_UserIdAndStatus(Long userId, VoteStatus voteState, Pageable pageable);

    //사용자의 게시글 개수
    Long countByUser_UserId(Long userId);

    //작성한 글 + 내가 선택글 관심사 + 팔로우한 사람의 글
    @Query(value = """
        (
          SELECT v.* FROM vote v 
          WHERE v.user_id = :userId AND v.status = 'PUBLISHED'
        )
        UNION
        (
          SELECT v.* FROM vote v 
          WHERE v.category_id IN (:categoryIds) AND v.status = 'PUBLISHED'
        )
        UNION
        (
          SELECT v.* FROM vote v 
          WHERE v.user_id IN (
            SELECT f.following_id FROM follow f WHERE f.follower_id = :userId
          ) AND v.status = 'PUBLISHED'
        )
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Vote> findMainPageVotesUnion(
            @Param("userId") Long userId,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
// 마감된글, 이미 투표한글은 제외
//    SELECT v FROM Vote v
//    WHERE (v.user.userId = :userId
//                    OR v.category.categoryId IN :categoryIds
//                    OR v.user.userId IN (
//                    SELECT f.followingId FROM Follow f WHERE f.followerId = :userId
//            ))
//    AND v.voteId NOT IN (
//            SELECT vs.vote.voteId FROM VoteSelection vs WHERE vs.user.userId = :userId
//    )
//    AND v.finishTime > CURRENT_TIMESTAMP
//    ORDER BY v.createdAt DESC

    //메인페이지 글 개수 count
    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT v.vote_id FROM vote v
            WHERE v.user_id = :userId
              AND v.status = 'PUBLISHED'
    
            UNION
    
            SELECT v.vote_id FROM vote v
            WHERE v.category_id IN (:categoryIds)
              AND v.status = 'PUBLISHED'
    
            UNION
    
            SELECT v.vote_id FROM vote v
            WHERE v.user_id IN (
                SELECT f.following_id FROM follow f WHERE f.follower_id = :userId
            )
              AND v.status = 'PUBLISHED'
        ) AS count_table
        """, nativeQuery = true)
    long countMainPageVotes(
            @Param("userId") Long userId,
            @Param("categoryIds") List<Long> categoryIds
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

    //좋아요 상위 글
    //추후 처리해야할 메서드
    @Query("""
        SELECT v
        FROM Vote v
        JOIN v.reactions r
        WHERE r.reaction = :reactionType
        GROUP BY v
        ORDER BY COUNT(r) DESC
    """)
    List<Vote> findByReactionTypeOrderByLikeCountDesc(
            @Param("reactionType") ReactionType reactionType,
            Pageable pageable
    );

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

    @Query("""
        SELECT DISTINCT v
        FROM Vote v
        LEFT JOIN FETCH v.selections s
    """)
    List<Vote> findAllWithSelections();

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

    // 최근 10개의 투표 조회
    List<Vote> findTop10ByUser_UserIdOrderByCreatedAtDesc(Long userId);

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
}
