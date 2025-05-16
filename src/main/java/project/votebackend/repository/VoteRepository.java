package project.votebackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.Vote;
import project.votebackend.type.ReactionType;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    //내가 작성한 글
    Page<Vote> findByUser_UserId(Long userId, Pageable pageable);

    //사용자의 게시글 개수
    Long countByUser_UserId(Long userId);

    //작성한 글 + 내가 선택한 관심사 + 팔로우한 사람의 글
    @Query(value = """
        (
          SELECT v.* FROM vote v 
          WHERE v.user_id = :userId
        )
        UNION
        (
          SELECT v.* FROM vote v 
          WHERE v.category_id IN (:categoryIds)
        )
        UNION
        (
          SELECT v.* FROM vote v 
          WHERE v.user_id IN (
            SELECT f.following_id FROM follow f WHERE f.follower_id = :userId
          )
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
        SELECT COUNT(DISTINCT v.vote_id)
        FROM vote v
        LEFT JOIN follow f ON f.following_id = v.user_id
        WHERE v.user_id = :userId
           OR v.category_id IN (:categoryIds)
           OR f.follower_id = :userId
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
        WHERE s.user.userId = :userId
        ORDER BY v.createdAt DESC
    """)
    Page<Vote> findVotedByUserId(@Param("userId") Long userId, Pageable pageable);

    //내가 좋아요한 글
    @EntityGraph(attributePaths = {
            "category", "user"
    })
    @Query("""
        SELECT DISTINCT v FROM Vote v
        JOIN v.reactions r
        WHERE r.user.userId = :userId AND r.reaction = 'LIKE'
        ORDER BY v.createdAt DESC
    """)
    Page<Vote> findLikedVotes(@Param("userId") Long userId, Pageable pageable);


    //내가 북마크한 글
    @EntityGraph(attributePaths = {
            "category", "user"
    })
    @Query("""
        SELECT DISTINCT v FROM Vote v
        JOIN v.reactions r
        WHERE r.user.userId = :userId AND r.reaction = 'BOOKMARK'
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
}
