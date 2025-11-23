package project.votebackend.repository.rank;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.votebackend.dto.user.UserMonthlyRankDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static project.votebackend.domain.user.QUser.user;
import static project.votebackend.domain.vote.QVote.vote;
import static project.votebackend.domain.vote.QVoteSelection.voteSelection;

@Repository
@RequiredArgsConstructor
public class RankQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<UserMonthlyRankDto> getMonthlyRanking(LocalDate month) {

        LocalDateTime start = month.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        return queryFactory
                .select(Projections.constructor(UserMonthlyRankDto.class,
                        user.userId,
                        user.username,
                        voteSelection.count()
                ))
                .from(voteSelection)
                .join(voteSelection.vote, vote)
                .join(vote.user, user)
                .where(voteSelection.createdAt.between(start, end))
                .groupBy(user.userId, user.username)
                .orderBy(voteSelection.count().desc())
                .fetch();
    }

}
