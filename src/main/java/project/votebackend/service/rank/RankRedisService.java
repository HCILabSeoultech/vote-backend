package project.votebackend.service.rank;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.rank.MonthlyUserRanking;
import project.votebackend.domain.user.User;
import project.votebackend.dto.user.UserMonthlyRankDto;
import project.votebackend.repository.rank.MonthlyUserRankingRepository;
import project.votebackend.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final MonthlyUserRankingRepository monthlyUserRankingRepository;

    private String getMonthlyKey(LocalDateTime time) {
        int year = time.getYear();
        int month = time.getMonthValue();
        return "monthlyRank:" + year + "-" + month;
    }

    // 득표 증가
    public void increaseUserScore(Long userId) {
        String key = getMonthlyKey(LocalDateTime.now());
        redisTemplate.opsForZSet().incrementScore(key, userId.toString(), 1.0);
    }

    // 득표 감소
    public void decreaseUserScore(Long userId) {
        String key = getMonthlyKey(LocalDateTime.now());
        redisTemplate.opsForZSet().incrementScore(key, userId.toString(), -1.0);
    }

    public List<UserMonthlyRankDto> getMonthlyRank(int limit) {

        String key = getMonthlyKey(LocalDateTime.now());

        Set<ZSetOperations.TypedTuple<String>> ranks =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);

        List<UserMonthlyRankDto> result = new ArrayList<>();

        if (ranks == null) return result;

        for (ZSetOperations.TypedTuple<String> tuple : ranks) {

            Long userId = Long.valueOf(tuple.getValue());
            Long totalVotes = tuple.getScore().longValue();

            String username = userRepository.findById(userId)
                    .map(User::getUsername)
                    .orElse("(탈퇴한 사용자)");

            result.add(new UserMonthlyRankDto(
                    userId,
                    username,
                    totalVotes
            ));
        }

        return result;
    }

    @Transactional
    public void settleMonthlyRanking(int year, int month) {

        String key = "monthlyRank:" + year + "-" + month;

        // Redis에서 전체 ZSET 조회
        Set<ZSetOperations.TypedTuple<String>> ranks =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);

        if (ranks == null || ranks.isEmpty()) {
            return;
        }

        for (ZSetOperations.TypedTuple<String> tuple : ranks) {

            Long userId = Long.valueOf(tuple.getValue());
            Long score = tuple.getScore().longValue();

            MonthlyUserRanking ranking = MonthlyUserRanking.builder()
                    .year(year)
                    .month(month)
                    .userId(userId)
                    .score(score)
                    .createdAt(LocalDateTime.now())
                    .build();

            monthlyUserRankingRepository.save(ranking);
        }

        // Redis 데이터 초기화
        redisTemplate.delete(key);
    }
}
