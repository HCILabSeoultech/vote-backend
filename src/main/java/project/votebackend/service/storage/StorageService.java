package project.votebackend.service.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.votebackend.domain.vote.Vote;
import project.votebackend.dto.vote.LoadVoteDto;
import project.votebackend.dto.vote.VoteSummaryDto;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.type.VoteStatus;
import project.votebackend.util.VoteStatisticsUtil;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final VoteRepository voteRepository;
    private final VoteStatisticsUtil voteStatisticsUtil;

    //참여한 게시물
    public List<VoteSummaryDto> getVotedPosts(Long userId, Pageable pageable) {
        Page<Vote> votes = voteRepository.findVotedByUserId(userId, pageable);
        return votes.stream()
                .map(VoteSummaryDto::from)
                .toList();
    }

    //북마크한 게시물
    public List<VoteSummaryDto> getBookmarkedPosts(Long userId, Pageable pageable) {
        Page<Vote> votes = voteRepository.findBookmarkedVotes(userId, pageable);
        return votes.stream()
                .map(VoteSummaryDto::from)
                .toList();
    }

    //내가 작성한 게시물
    public List<VoteSummaryDto> getCreatedPosts(Long userId, Pageable pageable) {
        Page<Vote> votes = voteRepository.findByUser_UserId(userId, pageable);
        return votes.stream()
                .map(VoteSummaryDto::from)
                .toList();
    }

    //내가 임시저장한 게시물
    public List<VoteSummaryDto> getDraftPosts(Long userId, Pageable pageable) {
        Page<Vote> votes = voteRepository.findByUser_UserIdAndStatus(userId, VoteStatus.DRAFT, pageable);
        return votes.stream()
                .map(VoteSummaryDto::from)
                .toList();
    }
}
