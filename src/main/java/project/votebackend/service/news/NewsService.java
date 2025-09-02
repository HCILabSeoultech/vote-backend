package project.votebackend.service.news;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.article.Cluster;
import project.votebackend.domain.reaction.NewsBookmark;
import project.votebackend.domain.user.User;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.ClusterException;
import project.votebackend.repository.article.ClusterRepository;
import project.votebackend.repository.news.NewsBookmarkRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.type.ErrorCode;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final ClusterRepository clusterRepository;
    private final UserRepository userRepository;
    private final NewsBookmarkRepository newsBookmarkRepository;

    //북마크 처리(토글)
    @Transactional
    public void bookmark(Long voteId, Long userId) {
        Cluster cluster = clusterRepository.findById(voteId)
                .orElseThrow(() -> new ClusterException(ErrorCode.CLUSTER_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        newsBookmarkRepository.findByUserAndCluster(user, cluster)
                .ifPresentOrElse(
                        newsBookmarkRepository::delete,
                        () -> {
                            NewsBookmark newsBookmark = NewsBookmark.builder()
                                    .cluster(cluster)
                                    .user(user)
                                    .build();
                            newsBookmarkRepository.save(newsBookmark);
                        }
                );
    }
}
