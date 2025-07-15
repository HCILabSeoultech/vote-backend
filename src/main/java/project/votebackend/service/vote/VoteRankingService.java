package project.votebackend.service.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.votebackend.domain.vote.Vote;
import project.votebackend.domain.vote.VoteImage;
import project.votebackend.domain.vote.VoteOption;
import project.votebackend.domain.vote.VoteStat6h;
import project.votebackend.dto.vote.TrendingVoteDto;
import project.votebackend.dto.vote.VoteSummaryDto;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.repository.voteStat.VoteStat6hRepository;
import project.votebackend.repository.voteStat.VoteStatHourlyRepository;
import project.votebackend.type.VoteStatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteRankingService {

//    private final VoteStat6hRepository voteStat6hRepository;
    private final VoteStatHourlyRepository voteStatHourlyRepository;
    private final VoteRepository voteRepository;

    // 투표에 등록된 첫 이미지 썸네일 반환
    private String extractThumbnail(Vote vote) {
        Optional<String> imageUrl = vote.getImages().stream()
                .findFirst()
                .map(VoteImage::getImageUrl); // Optional<String>

        if (imageUrl.isPresent()) {
            return imageUrl.get();
        }

        // 이미지가 없다면 옵션 이미지 중 첫 번째
        return vote.getOptions().stream()
                .map(VoteOption::getOptionImage)
                .filter(img -> img != null && !img.isEmpty())
                .findFirst()
                .orElse(null); // 또는 기본 이미지 URL
    }

    // 총 투표수 기준 정렬
    public List<VoteSummaryDto> getVotesSortedByTotalVotes(VoteStatusType status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Vote> sortedVotes = voteRepository.findVotesSortedByTotalVotes(status.name(), pageable);
        return sortedVotes.stream().map(VoteSummaryDto::from).toList();
    }

    // 좋아요순 기준 정렬
    public List<VoteSummaryDto> getVotesSortedByLikes(VoteStatusType status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Vote> votes = voteRepository.findVotesSortedByLikes(status.name(), pageable);
        return votes.stream().map(VoteSummaryDto::from).toList();
    }

    // 댓글수 기준 정렬
    public List<VoteSummaryDto> getVotesSortedByComments(VoteStatusType status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Vote> votes = voteRepository.findVotesSortedByComments(status.name(), pageable);
        return votes.stream().map(VoteSummaryDto::from).toList();
    }


//    // 전체 득표순 정렬 (진행 중/종료 상태에 따라 분기)
//    public List<VoteSummaryDto> getVotesSortedByTotalVotes(VoteStatusType status, int page, int size) {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime latest = voteStat6hRepository.findLatestStatTime();
//        PageRequest pageable = PageRequest.of(page, size);
//
//        if (status == VoteStatusType.ONGOING) {
//            // 진행 중인 투표만
//            return voteStat6hRepository
//                    .findByStatTimeAndVote_FinishTimeAfterOrderByTotalVoteCountDesc(latest, now, pageable)
//                    .stream().map(this::toDto).toList();
//        } else if (status == VoteStatusType.ENDED) {
//            // 종료 된 투표만
//            return voteStat6hRepository
//                    .findByStatTimeAndVote_FinishTimeBeforeOrderByTotalVoteCountDesc(latest, now, pageable)
//                    .stream().map(this::toDto).toList();
//        } else {
//            // 전체 (전체)
//            return voteStat6hRepository
//                    .findByStatTimeOrderByTotalVoteCountDesc(latest, pageable)
//                    .stream().map(this::toDto).toList();
//        }
//    }
//
//    // 오늘 득표순 정렬 (진행 중 투표만)
//    public List<VoteSummaryDto> getVotesSortedByTodayVotes(int page, int size) {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime latest = voteStat6hRepository.findLatestStatTime();
//        PageRequest pageable = PageRequest.of(page, size);
//
//        return voteStat6hRepository.findByStatTimeAndVote_FinishTimeAfterOrderByTodayVoteCountDesc(latest, now, pageable)
//                .stream().map(this::toDto).toList();
//    }
//
//    // 댓글 수 기준 정렬 (진행 중/종료 상태에 따라 분기)
//    public List<VoteSummaryDto> getVotesSortedByComments(VoteStatusType status, int page, int size) {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime latest = voteStat6hRepository.findLatestStatTime();
//        PageRequest pageable = PageRequest.of(page, size);
//
//        if (status == VoteStatusType.ONGOING) {
//            // 진행 중인 투표만
//            return voteStat6hRepository
//                    .findByStatTimeAndVote_FinishTimeAfterOrderByCommentCountDesc(latest, now, pageable)
//                    .stream().map(this::toDto).toList();
//        } else if (status == VoteStatusType.ENDED) {
//            // 종료 된 투표만
//            return voteStat6hRepository
//                    .findByStatTimeAndVote_FinishTimeBeforeOrderByCommentCountDesc(latest, now, pageable)
//                    .stream().map(this::toDto).toList();
//        } else {
//            // 전체
//            return voteStat6hRepository
//                    .findByStatTimeOrderByCommentCountDesc(latest, pageable)
//                    .stream().map(this::toDto).toList();
//        }
//    }

    // 관심 급등순 정렬 (최근 1시간 투표 수 기준, 진행 중만)
    public List<TrendingVoteDto> getTrendingVotes(int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime latest = voteStatHourlyRepository.findLatestStatHour();
        PageRequest pageable = PageRequest.of(page, size);

        return voteStatHourlyRepository.findByStatHourAndVote_FinishTimeAfterOrderByVoteCountDesc(latest, now, pageable)
                .stream().map(stat -> {
                    Vote vote = stat.getVote();

                    int todayVotes = (int) vote.getSelections().stream()
                            .filter(s -> s.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                            .count();

                    return TrendingVoteDto.builder()
                            .voteId(vote.getVoteId())
                            .title(vote.getTitle())
                            .thumbnailImageUrl(extractThumbnail(vote))
                            .hourlyVoteCount(stat.getVoteCount())
                            .hourlyRank(stat.getRank())
                            .hourlyRankChange(stat.getRankChange())
                            .totalVotes(vote.getSelections().size())
                            .todayVotes(todayVotes)
                            .commentCount((int) vote.getComments().stream().filter(c -> c.getParent() == null).count())
                            .finishTime(vote.getFinishTime())
                            .build();
                }).toList();
    }

//    // VoteStat6h → VoteSummaryDto 매핑
//    private VoteSummaryDto toDto(VoteStat6h stat) {
//        Vote vote = stat.getVote();
//        return VoteSummaryDto.builder()
//                .voteId(vote.getVoteId())
//                .title(vote.getTitle())
//                .thumbnailImageUrl(extractThumbnail(vote))
//                .totalVotes(stat.getTotalVoteCount())
//                .todayVotes(stat.getTodayVoteCount())
//                .commentCount(stat.getCommentCount())
//                .rankTotal(stat.getRankTotal())
//                .rankToday(stat.getRankToday())
//                .rankComment(stat.getRankComment())
//                .rankChangeTotal(stat.getRankChangeTotal())
//                .rankChangeToday(stat.getRankChangeToday())
//                .rankChangeComment(stat.getRankChangeComment())
//                .ongoingCommentRank(stat.getOngoingCommentRank())
//                .ongoingCommentRankChange(stat.getOngoingCommentRankChange())
//                .ongoingVoteCountRank(stat.getOngoingVoteCountRank())
//                .ongoingVoteCountRankChange(stat.getOngoingVoteCountRankChange())
//                .endedCommentRank(stat.getEndedCommentRank())
//                .endedCommentRankChange(stat.getEndedCommentRankChange())
//                .endedVoteCountRank(stat.getEndedVoteCountRank())
//                .endedVoteCountRankChange(stat.getEndedVoteCountRankChange())
//                .finishTime(vote.getFinishTime())
//                .build();
//    }
}
