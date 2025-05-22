package project.votebackend.repository.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.comment.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    //부모 댓글
    Page<Comment> findByVote_VoteIdAndParentIsNull(Long voteId, Pageable pageable);

    //자식 댓글
    List<Comment> findByParent_CommentIdOrderByCreatedAtAsc(Long parentId);

    //댓글 수 카운트
    @Query(value = """
        SELECT vote_id, COUNT(*) AS comment_count
        FROM comment
        WHERE parent_id IS NULL AND vote_id IN :voteIds
        GROUP BY vote_id
    """, nativeQuery = true)
    List<Object[]> countParentCommentsByVoteIds(@Param("voteIds") List<Long> voteIds);


}
