package project.votebackend.repository.vote;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.follow.QFollow;
import project.votebackend.domain.vote.QVote;
import project.votebackend.domain.vote.QVoteSelection;
import project.votebackend.domain.vote.Vote;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static project.votebackend.type.VoteStatus.PUBLISHED;

@Repository
@RequiredArgsConstructor
public class MainPageVoteRepository {

    private final JPAQueryFactory queryFactory;

    public List<Vote> findMainPageVotesUnion(
            Long userId,
            List<Long> categoryIds,
            int limit,
            int offset,
            Long aiUserId
    ) {
        QVote v = QVote.vote;
        QVoteSelection s = QVoteSelection.voteSelection;
        QFollow f = QFollow.follow;

        // 공통 조건: PUBLISHED + 진행중 + AI 계정 제외
        BooleanExpression baseCond =
                v.status.eq(PUBLISHED)
                        .and(v.user.userId.ne(aiUserId))
                        .and(v.finishTime.isNull()
                                .or(v.finishTime.after(LocalDateTime.now())));

        // ===== 1. 내가 작성한 글 =====
        List<Vote> myVotes = queryFactory
                .select(v)
                .from(v)
                .where(baseCond.and(v.user.userId.eq(userId)))
                .fetch();

        // ===== 2. 관심 카테고리 글 =====
        List<Vote> categoryVotes = queryFactory
                .select(v)
                .from(v)
                .where(baseCond.and(v.category.categoryId.in(categoryIds)))
                .fetch();

        // ===== 3. 팔로우한 사람 글 =====
        List<Long> followings = queryFactory
                .select(f.following.userId)
                .from(f)
                .where(f.follower.userId.eq(userId))
                .fetch();

        List<Vote> followVotes = followings.isEmpty()
                ? List.of()
                : queryFactory
                .select(v)
                .from(v)
                .where(baseCond.and(v.user.userId.in(followings)))
                .fetch();

        // ===== UNION (중복 제거) =====
        Set<Long> used = new HashSet<>();
        List<Vote> union = new ArrayList<>();

        for (Vote vote : myVotes) if (used.add(vote.getVoteId())) union.add(vote);
        for (Vote vote : categoryVotes) if (used.add(vote.getVoteId())) union.add(vote);
        for (Vote vote : followVotes) if (used.add(vote.getVoteId())) union.add(vote);

        // ===== 정렬 =====
        union.sort((a, b) -> {
            // 1) priority (calcPr)
            int cmp = Integer.compare(
                    calcPr(a, userId),
                    calcPr(b, userId)
            );
            if (cmp != 0) return -cmp; // 내림차순

            // 2) createdAt DESC
            cmp = b.getCreatedAt().compareTo(a.getCreatedAt());
            if (cmp != 0) return cmp;

            // 3) voteId DESC
            return Long.compare(b.getVoteId(), a.getVoteId());
        });

        // ===== LIMIT / OFFSET =====
        int fromIndex = Math.min(offset, union.size());
        int toIndex = Math.min(offset + limit, union.size());

        return union.subList(fromIndex, toIndex);
    }

    /**
     * priority 계산
     * 진행중 + 미참여 = 2
     * 진행중 + 참여   = 1
     * 그 외 = 0
     */
    private int calcPr(Vote v, Long userId) {
        boolean isOpen = v.getFinishTime() == null || v.getFinishTime().isAfter(LocalDateTime.now());
        boolean participated = v.getSelections().stream().anyMatch(s -> s.getUser().getUserId().equals(userId));

        if (isOpen && !participated) return 2;
        if (isOpen) return 1;
        return 0;
    }

    // AI 글
    public List<Vote> findAiCandidatesForCategories(
            Long userId,
            List<Long> categoryIds,
            int limit,
            Long aiUserId
    ) {
        QVote v = QVote.vote;
        QVoteSelection s = QVoteSelection.voteSelection;

        return queryFactory
                .select(v)
                .from(v)
                .where(
                        v.status.eq(PUBLISHED)
                                .and(v.category.categoryId.in(categoryIds))
                                .and(v.user.userId.eq(aiUserId))
                                .and(v.finishTime.isNull()
                                        .or(v.finishTime.after(LocalDateTime.now())))
                                .and(
                                        JPAExpressions.selectOne()
                                                .from(s)
                                                .where(s.vote.eq(v), s.user.userId.eq(userId))
                                                .notExists()
                                )
                )
                .orderBy(
                        v.createdAt.desc(),
                        v.voteId.desc()
                )
                .limit(limit)
                .fetch();
    }


    // 인기
    public List<Vote> findPopularCandidatesGlobal(
            Long userId,
            Pageable pageable
    ) {
        QVote v = QVote.vote;
        QVoteSelection s = QVoteSelection.voteSelection;

        BooleanExpression participated = JPAExpressions
                .selectOne()
                .from(s)
                .where(s.vote.eq(v), s.user.userId.eq(userId))
                .exists();

        BooleanExpression notParticipated = JPAExpressions
                .selectOne()
                .from(s)
                .where(s.vote.eq(v), s.user.userId.eq(userId))
                .notExists();

        NumberExpression<Integer> pr = new CaseBuilder()
                .when(notParticipated).then(2)
                .when(participated).then(1)
                .otherwise(0);

        NumberExpression<Long> participantCount =
                Expressions.numberTemplate(Long.class,
                        "(select count(*) from VoteSelection vs where vs.user.userId = {0})",
                        v.voteId);

        return queryFactory
                .select(v)
                .from(v)
                .where(
                        v.status.eq(PUBLISHED)
                                .and(v.finishTime.isNull()
                                        .or(v.finishTime.after(LocalDateTime.now())))
                )
                .orderBy(
                        pr.desc(),
                        participantCount.desc(),
                        v.createdAt.desc(),
                        v.voteId.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    // 관심 카테고리를 제외한 글
    public List<Vote> findOtherCategoryCandidates(
            List<Long> myCategoryIds,
            int limit,
            int offset,
            Long aiUserId
    ) {
        QVote v = QVote.vote;

        return queryFactory
                .select(v)
                .from(v)
                .where(
                        v.status.eq(PUBLISHED)
                                .and(v.user.userId.ne(aiUserId))
                                .and(v.category.categoryId.notIn(myCategoryIds))
                                .and(v.finishTime.isNull()
                                        .or(v.finishTime.after(LocalDateTime.now())))
                )
                .orderBy(
                        v.createdAt.desc(),
                        v.voteId.desc()
                )
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}
