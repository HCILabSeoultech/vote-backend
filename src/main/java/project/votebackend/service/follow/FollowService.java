package project.votebackend.service.follow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.follow.Follow;
import project.votebackend.domain.user.User;
import project.votebackend.dto.follow.FollowUserDto;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.FollowException;
import project.votebackend.repository.follow.FollowRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.type.ErrorCode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
}
