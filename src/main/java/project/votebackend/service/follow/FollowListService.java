package project.votebackend.service.follow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.votebackend.domain.follow.Follow;
import project.votebackend.domain.user.User;
import project.votebackend.dto.follow.FollowUserDto;
import project.votebackend.exception.AuthException;
import project.votebackend.repository.follow.FollowRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.type.ErrorCode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowListService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    // 나를 팔로우한 사람 목록 조회
    public List<FollowUserDto> getFollowers(Long userId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // 1. 나를 팔로우하는 Follow 엔티티 목록 가져오기
        List<Follow> followers = followRepository.findByFollowing(me);

        // 2. 팔로워들의 userId 추출
        List<Long> followerIds = followers.stream()
                .map(f -> f.getFollower().getUserId())
                .toList();

        // 3. 빈 리스트 방어
        if (followerIds.isEmpty()) {
            return List.of();
        }

        // 4. 내가 그들을 다시 팔로우하고 있는지 조회
        List<Follow> myFollowings = followRepository.findByFollower_UserIdAndFollowing_UserIdIn(userId, followerIds);
        Set<Long> iFollowSet = myFollowings.stream()
                .map(f -> f.getFollowing().getUserId())
                .collect(Collectors.toSet());

        // 5. DTO 매핑
        return followers.stream()
                .map(f -> {
                    User follower = f.getFollower();
                    return FollowUserDto.builder()
                            .userId(follower.getUserId())
                            .name(follower.getName())
                            .profileImage(follower.getProfileImage())
                            .introduction(follower.getIntroduction())
                            .isFollowing(iFollowSet.contains(follower.getUserId()))
                            .build();
                })
                .toList();
    }

    // 내가 팔로우한 사람 목록 조회
    public List<FollowUserDto> getFollowings(Long userId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // 1. 내가 팔로우하는 Follow 엔티티 목록 가져오기
        List<Follow> followings = followRepository.findByFollower(me);

        // 2. 팔로잉들의 userId 추출
        List<Long> followingIds = followings.stream()
                .map(f -> f.getFollowing().getUserId())
                .toList();

        // 3. 빈 리스트 방어
        if (followingIds.isEmpty()) {
            return List.of();
        }

        // 5. DTO 매핑
        return followings.stream()
                .map(f -> {
                    User following = f.getFollowing();
                    return FollowUserDto.builder()
                            .userId(following.getUserId())
                            .name(following.getName())
                            .profileImage(following.getProfileImage())
                            .introduction(following.getIntroduction())
                            .isFollowing(true)
                            .build();
                })
                .toList();
    }
}
