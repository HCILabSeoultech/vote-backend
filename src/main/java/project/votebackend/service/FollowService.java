package project.votebackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.Follow;
import project.votebackend.domain.User;
import project.votebackend.dto.FollowUserDto;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.FollowException;
import project.votebackend.repository.FollowRepository;
import project.votebackend.repository.UserRepository;
import project.votebackend.type.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    // 유저네임을 통한 id값 get
    private Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND))
                .getUserId();
    }

    //팔로우
    @Transactional
    public Long follow(String username, Long followingId) {
        Long followerId = getUserIdByUsername(username);

        // 중복 팔로우 검사
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        followRepository.findByFollowerAndFollowing(follower, following)
                .ifPresent(f -> {
                    try {
                        throw new FollowException(ErrorCode.ALREADY_FOLLOW);
                    } catch (FollowException e) {
                        throw new RuntimeException(e);
                    }
                });

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
        return follow.getFollowId();
    }

    //언팔로우
    @Transactional
    public void unfollow(String username, Long followingId) {
        User follower = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    //팔로우 여부 확인
    public boolean isFollowing(String username, Long followingId) {
        User follower = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        return followRepository.findByFollowerAndFollowing(follower, following).isPresent();
    }

    //나를 팔로우한 사람 목록 조회
    public List<FollowUserDto> getFollowers(Long userId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        List<Follow> followers = followRepository.findByFollowing(me);

        return followers.stream()
                .map(f -> {
                    User follower = f.getFollower();
                    return FollowUserDto.builder()
                            .userId(follower.getUserId())
                            .username(follower.getUsername())
                            .profileImage(follower.getProfileImage())
                            .build();
                })
                .toList();
    }

    //내가 팔로우한 사람 목록 조회
    public List<FollowUserDto> getFollowings(Long userId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        List<Follow> followings = followRepository.findByFollower(me);

        return followings.stream()
                .map(f -> {
                    User following = f.getFollowing();
                    return FollowUserDto.builder()
                            .userId(following.getUserId())
                            .username(following.getUsername())
                            .profileImage(following.getProfileImage())
                            .build();
                })
                .toList();
    }
}
