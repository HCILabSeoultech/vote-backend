package project.votebackend.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.search.NewsSearch;
import project.votebackend.domain.user.User;
import project.votebackend.dto.article.ClusterSummaryDto;
import project.votebackend.dto.search.SearchDto;
import project.votebackend.dto.vote.VoteSearchResponse;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.ClusterException;
import project.votebackend.repository.article.ClusterQueryRepository;
import project.votebackend.repository.article.ClusterRepository;
import project.votebackend.repository.news.NewsSearchRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.vote.VoteQueryRepository;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.type.Category;
import project.votebackend.type.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final NewsSearchRepository newsSearchRepository;
    private final UserRepository userRepository;
    private final VoteQueryRepository voteQueryRepository;
    private final ClusterQueryRepository clusterQueryRepository;

    public Page<VoteSearchResponse> searchVotes(String keyword, Pageable pageable) {
        return voteQueryRepository.searchVotes(keyword, pageable);
    }

    public Page<ClusterSummaryDto> searchNews(String keyword, Pageable pageable, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // 검색 키워드 저장
        newsSearchRepository.save(
                NewsSearch.builder()
                        .user(user)
                        .keyword(keyword)
                        .build()
        );

        return clusterQueryRepository.searchClusterSummaries(keyword, pageable);
    }

    @Transactional
    public void deleteOne(Long userId, Long searchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        NewsSearch newsSearch = newsSearchRepository.findById(searchId)
                        .orElseThrow(() -> new ClusterException(ErrorCode.SEARCH_NOT_FOUND));

        newsSearchRepository.deleteBySearchIdAndUser_UserId(searchId, userId);
    }

    @Transactional
    public void deleteAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        newsSearchRepository.deleteByUser_UserId(userId);
    }

    public List<SearchDto> getSearchList(Long userId) {
        return newsSearchRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(SearchDto::fromEntity)
                .toList();
    }
}
