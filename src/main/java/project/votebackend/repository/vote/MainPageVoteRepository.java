package project.votebackend.repository.vote;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.follow.QFollow;
import project.votebackend.domain.vote.QVote;
import project.votebackend.domain.vote.QVoteSelection;
import project.votebackend.domain.vote.Vote;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MainPageVoteRepository {

    private final JPAQueryFactory queryFactory;

    public List<Vote> findMainFeedUnified(
            Long userId,
            List<Long> categoryIds,
            int limit,
            Long aiUserId
    ) {
        QVote v = QVote.vote;
        QVoteSelection s = QVoteSelection.voteSelection;
        QFollow f = QFollow.follow;

        // 팔로잉 ID
        List<Long> followings = queryFactory
                .select(f.following.userId)
                .from(f)
                .where(f.follower.userId.eq(userId))
                .fetch();

        // 참여 여부 표현
        NumberExpression<Integer> participated = new CaseBuilder()
                .when(
                        JPAExpressions.selectOne()
                                .from(s)
                                .where(
                                        s.vote.eq(v),
                                        s.user.userId.eq(userId)
                                )
                                .exists())
                .then(1)
                .otherwise(0);

        // 기본 score (내 글 > 관심 카테고리 > 팔로우 > AI > 기타)
        NumberExpression<Integer> score = new CaseBuilder()
                .when(v.user.userId.eq(userId)).then(50)
                .when(v.category.categoryId.in(categoryIds)).then(40)
                .when(v.user.userId.in(followings)).then(30)
                .when(v.user.userId.eq(aiUserId)).then(20)
                .otherwise(10);

        // 정렬 우선순위
        OrderSpecifier<?> orderByScore = score.desc();
        OrderSpecifier<?> orderByParticipated = participated.asc(); // 미참여 우선
        OrderSpecifier<?> orderByCreatedAt = v.createdAt.desc();
        OrderSpecifier<?> orderByVoteId = v.voteId.desc();

        return queryFactory
                .select(v)
                .from(v)
                .where(
                        v.status.eq(project.votebackend.type.VoteStatus.PUBLISHED)
                                .and(
                                        v.finishTime.isNull()
                                                .or(v.finishTime.after(LocalDateTime.now()))
                                )
                )
                .orderBy(
                        orderByScore,
                        orderByParticipated,
                        orderByCreatedAt,
                        orderByVoteId
                )
                .limit(limit)
                .fetch();
    }
}
