package project.votebackend.service.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.vote.Vote;
import project.votebackend.domain.vote.VoteStat6h;
import project.votebackend.domain.vote.VoteStatHourly;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.repository.voteStat.VoteStat6hRepository;
import project.votebackend.repository.voteStat.VoteStatHourlyRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteSchedulingService {

    private final VoteStat6hRepository voteStat6hRepository;
    private final VoteStatHourlyRepository voteStatHourlyRepository;
    private final VoteRepository voteRepository;

    /**
     * [6시간 단위 통계 생성]
     * - 현재 시간을 기준으로 누적 투표 수, 오늘의 투표 수, 댓글 수 통계를 계산
     * - 각 항목에 대해 랭킹을 계산 (공동 순위 고려)
     * - 이전 랭킹과 비교하여 순위 변동 계산
     * - 이전 통계는 삭제
     */
    @Transactional
    public void generate6hStats() {
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0); // 정시 기준

        // 모든 투표 + 선택지 로딩
        List<Vote> votes = voteRepository.findAllWithSelections();

        // 각 투표에 대한 통계 계산
        List<VoteStat6h> statList = votes.stream().map(vote -> {
            int totalVotes = vote.getSelections().size(); // 전체 투표 수
            int todayVotes = (int) vote.getSelections().stream()
                    .filter(s -> s.getCreatedAt().toLocalDate().equals(now.toLocalDate()))
                    .count(); // 오늘 투표 수
            int commentCount = (int) vote.getComments().stream()
                    .filter(c -> c.getParent() == null)
                    .count(); // 댓글 수

            return VoteStat6h.builder()
                    .vote(vote)
                    .statTime(now)
                    .totalVoteCount(totalVotes)
                    .todayVoteCount(todayVotes)
                    .commentCount(commentCount)
                    .build();
        }).collect(Collectors.toList());

        // 랭킹 계산 (득표수/오늘득표/댓글수 기준)
        assignRanks(statList, Comparator.comparingInt(VoteStat6h::getTotalVoteCount).reversed(), "total");
        assignRanks(statList, Comparator.comparingInt(VoteStat6h::getTodayVoteCount).reversed(), "today");
        assignRanks(statList, Comparator.comparingInt(VoteStat6h::getCommentCount).reversed(), "comment");

        // 직전 통계와 비교하여 순위 변화 계산
        LocalDateTime lastTime = voteStat6hRepository.findLatestStatTimeBefore(now).orElse(null);
        Map<Long, VoteStat6h> prevStatMap = lastTime != null
                ? voteStat6hRepository.findByStatTime(lastTime).stream().collect(
                Collectors.toMap(
                        stat -> stat.getVote().getVoteId(),
                        stat -> stat,
                        (s1, s2) -> s1
                )
        )
                : Collections.emptyMap();

        for (VoteStat6h stat : statList) {
            VoteStat6h prev = prevStatMap.get(stat.getVote().getVoteId());

            // 각 기준에 대해 랭크 변화 설정
            stat.setRankChangeTotal(prev != null ? prev.getRankTotal() - stat.getRankTotal() : 0);
            stat.setRankChangeToday(prev != null ? prev.getRankToday() - stat.getRankToday() : 0);
            stat.setRankChangeComment(prev != null ? prev.getRankComment() - stat.getRankComment() : 0);

            voteStat6hRepository.save(stat);
        }

        // 직전 기록 삭제 (6시간치 보관 정책)
        if (lastTime != null) {
            voteStat6hRepository.deleteByStatTime(lastTime);
        }
    }

    /**
     * 랭킹 할당 함수 (공동 순위 적용)
     */
    private void assignRanks(List<VoteStat6h> stats, Comparator<VoteStat6h> comparator, String type) {
        stats.sort(comparator); // 정렬

        int rank = 1;
        int prevScore = -1;

        for (int i = 0; i < stats.size(); i++) {
            VoteStat6h stat = stats.get(i);
            int score = switch (type) {
                case "total" -> stat.getTotalVoteCount();
                case "today" -> stat.getTodayVoteCount();
                case "comment" -> stat.getCommentCount();
                default -> throw new IllegalArgumentException("Invalid type");
            };

            if (score != prevScore) {
                rank = i + 1;
            }

            switch (type) {
                case "total" -> stat.setRankTotal(rank);
                case "today" -> stat.setRankToday(rank);
                case "comment" -> stat.setRankComment(rank);
            }

            prevScore = score;
        }
    }

    /**
     * [1시간 단위 통계 생성]
     * - 최근 1시간 동안의 투표 수를 계산하여 저장
     * - 공동 순위 고려하여 등수 계산
     * - 이전 시간 대비 순위 변화도 기록
     */
    @Transactional
    public void generateHourlyStats() {
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0); // 정시
        LocalDateTime oneHourAgo = now.minusHours(1); // 1시간 전

        // 투표 + 선택지 로딩
        List<Vote> votes = voteRepository.findAllWithSelections();

        // 최근 1시간 투표 수 계산
        List<VoteStatHourly> statList = votes.stream().map(vote -> {
            int voteCount = (int) vote.getSelections().stream()
                    .filter(s -> {
                        LocalDateTime created = s.getCreatedAt();
                        return created.isAfter(oneHourAgo) && !created.isAfter(now);
                    }).count();

            return VoteStatHourly.builder()
                    .vote(vote)
                    .statHour(now)
                    .voteCount(voteCount)
                    .createdAt(now)
                    .build();
        }).collect(Collectors.toList());

        // 공동 순위 계산
        statList.sort(Comparator.comparingInt(VoteStatHourly::getVoteCount).reversed());
        int rank = 1;
        int prevScore = -1;

        for (int i = 0; i < statList.size(); i++) {
            VoteStatHourly stat = statList.get(i);
            int score = stat.getVoteCount();

            if (score != prevScore) {
                rank = i + 1;
            }

            stat.setRank(rank);
            prevScore = score;
        }

        // 직전 1시간 통계 불러오기
        LocalDateTime lastHour = voteStatHourlyRepository.findLatestStatTimeBefore(now).orElse(null);
        Map<Long, VoteStatHourly> prevMap = lastHour != null
                ? voteStatHourlyRepository.findByStatHour(lastHour).stream().collect(
                Collectors.toMap(
                        s -> s.getVote().getVoteId(),
                        s -> s,
                        (s1, s2) -> s1
                )
        )
                : Collections.emptyMap();

        // 순위 변화 계산
        for (VoteStatHourly stat : statList) {
            VoteStatHourly prev = prevMap.get(stat.getVote().getVoteId());
            stat.setRankChange(prev != null ? prev.getRank() - stat.getRank() : 0);
            voteStatHourlyRepository.save(stat);
        }

        // 이전 시간 통계 삭제
        if (lastHour != null) {
            voteStatHourlyRepository.deleteByStatHour(lastHour);
        }
    }
}
