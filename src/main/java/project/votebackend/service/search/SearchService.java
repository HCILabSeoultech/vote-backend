package project.votebackend.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.votebackend.domain.search.NewsSearch;
import project.votebackend.domain.user.User;
import project.votebackend.dto.article.ClusterSummaryDto;
import project.votebackend.dto.vote.VoteSearchResponse;
import project.votebackend.exception.AuthException;
import project.votebackend.repository.article.ClusterRepository;
import project.votebackend.repository.news.NewsSearchRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.type.ErrorCode;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final VoteRepository voteRepository;
    private final ClusterRepository clusterRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final UserRepository userRepository;

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

    public Page<ClusterSummaryDto> searchNews(String keyword, Pageable pageable, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        newsSearchRepository.save(
                NewsSearch.builder()
                        .user(user)
                        .keyword(keyword)
                        .build()
        );

        Page<Object[]> page = clusterRepository.searchClusterSummaries(keyword, pageable);

        return page.map(row -> new ClusterSummaryDto(
                ((Number) row[0]).longValue(),                 // cluster_id → id
                (String) row[1],                               // image_url → imageUrl
                (String) row[2],                               // title
                ((java.sql.Timestamp) row[3]).toLocalDateTime()// created_at → createdAt
        ));
    }
}
