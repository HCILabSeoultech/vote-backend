package project.votebackend.service.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.votebackend.domain.vote.Vote;
import project.votebackend.dto.vote.LoadVoteDto;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.util.VoteStatisticsUtil;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final VoteRepository voteRepository;
    private final VoteStatisticsUtil voteStatisticsUtil;


    //내가 투표한 게시물
    public Page<LoadVoteDto> getVotedPosts(Long userId, Pageable pageable) {
        Page<Vote> votes = voteRepository.findVotedByUserId(userId, pageable);
        List<Long> voteIds = votes.getContent().stream().map(Vote::getVoteId).toList();
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, voteIds);
        return voteStatisticsUtil.getLoadVoteDtos(userId, votes, stats, pageable);
    }

    //북마크한 게시물
    public Page<LoadVoteDto> getBookmarkedPosts(Long userId, Pageable pageable) {
        Page<Vote> votes = voteRepository.findBookmarkedVotes(userId, pageable);
        List<Long> voteIds = votes.getContent().stream().map(Vote::getVoteId).toList();
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, voteIds);
        return voteStatisticsUtil.getLoadVoteDtos(userId, votes, stats, pageable);
    }

    //내가 작성한 게시물
    public Page<LoadVoteDto> getCreatedPosts(Long userId, Pageable pageable) {
        Page<Vote> votes = voteRepository.findByUser_UserId(userId, pageable);
        List<Long> voteIds = votes.getContent().stream().map(Vote::getVoteId).toList();
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, voteIds);
        return voteStatisticsUtil.getLoadVoteDtos(userId, votes, stats, pageable);
    }
}
