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

    private final VoteStatHourlyRepository voteStatHourlyRepository;
    private final VoteRepository voteRepository;

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

    // 인기 게시글 정렬
    public List<VoteSummaryDto> getTrendingVotes(VoteStatusType status, int page, int size) {
        int offset = page * size;
        List<Vote> votes = voteRepository.findVotesByTrending(status.name(), size, offset);
        return votes.stream().map(VoteSummaryDto::from).toList();
    }
}
