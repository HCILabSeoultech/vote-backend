package project.votebackend.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.votebackend.dto.vote.VoteSearchResponse;
import project.votebackend.repository.vote.VoteRepository;


@Service
@RequiredArgsConstructor
public class SearchService {

    private final VoteRepository voteRepository;

    public Page<VoteSearchResponse> searchVotes(String keyword, Pageable pageable) {
        Page<Object[]> page = voteRepository.searchVotesWithStats(keyword, pageable);
        return page.map(row -> new VoteSearchResponse(
                ((Number) row[0]).longValue(),  // vote_id
                (String) row[1],                // title
                ((Number) row[2]).intValue(),   // participant_count
                ((Number) row[3]).intValue(),   // like_count
                ((Number) row[4]).intValue()    // comment_count
        ));
    }
}
