package project.votebackend.repository.vote;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import project.votebackend.dto.vote.VoteSearchResponse;
import project.votebackend.type.ReactionType;

import java.util.List;

import static project.votebackend.domain.comment.QComment.comment;
import static project.votebackend.domain.reaction.QReaction.reaction1;
import static project.votebackend.domain.vote.QVote.vote;
import static project.votebackend.domain.vote.QVoteSelection.voteSelection;

@Repository
@RequiredArgsConstructor
public class VoteQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<VoteSearchResponse> searchVotes(String keyword, Pageable pageable) {

        // 본문 쿼리
        List<VoteSearchResponse> results = queryFactory
                .select(Projections.constructor(
                        VoteSearchResponse.class,
                        vote.voteId,
                        vote.title,
                        voteSelection.countDistinct().coalesce(0L), // totalVotes (참여자 수)
                        reaction1.reactionId.count().coalesce(0L),     // likeCount (LIKE 수)
                        comment.commentId.count().coalesce(0L)       // commentCount (최상위 댓글 수)
                ))
                .from(vote)
                .leftJoin(voteSelection).on(voteSelection.vote.eq(vote))
                .leftJoin(reaction1).on(reaction1.vote.eq(vote)
                        .and(reaction1.reaction.eq(ReactionType.LIKE)))
                .leftJoin(comment).on(comment.vote.eq(vote)
                        .and(comment.parent.isNull()))
                .where(vote.title.containsIgnoreCase(keyword))
                .groupBy(vote.voteId, vote.title)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        Long total = queryFactory
                .select(vote.count())
                .from(vote)
                .where(vote.title.containsIgnoreCase(keyword))
                .fetchOne();

        return new PageImpl<>(results, pageable, total == null ? 0 : total);
    }
}

