package project.votebackend.repository.follow;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.follow.Follow;
import project.votebackend.domain.user.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    void deleteByFollowerAndFollowing(User follower, User following);
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    Long countByFollowing(User following); //나를 팔로우하는 사람 수
    Long countByFollower(User follower); //내가 팔로잉하는 사람 수

    // 나를 팔로우한 사람들
    // 팔로워 목록 조회 시 N+1 방지
    @EntityGraph(attributePaths = "follower")
    List<Follow> findByFollowing(User following);

    // 내가 팔로잉한 사람들
    List<Follow> findByFollower(User user);

    // 내가 특정 유저들을 팔로우하고 있는지 여부 확인용 (userId → follower, followerIds → following)
    List<Follow> findByFollower_UserIdAndFollowing_UserIdIn(Long followerId, List<Long> followingIds);

}
