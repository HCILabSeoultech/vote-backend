package project.votebackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.Follow;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.FollowException;
import project.votebackend.repository.FollowRepository;
import project.votebackend.repository.UserRepository;
import project.votebackend.type.ErrorCode;

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

        followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .ifPresent(f -> {
                    try {
                        throw new FollowException(ErrorCode.ALREADY_FOLLOW);
                    } catch (FollowException e) {
                        throw new RuntimeException(e);
                    }
                });

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();

        followRepository.save(follow);
        return follow.getFollowId();
    }

    //언팔로우
    @Transactional
    public void unfollow(String username, Long followingId) {
        Long followerId = getUserIdByUsername(username);
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    //팔로우 여부 확인
    public boolean isFollowing(String username, Long followingId) {
        Long followerId = getUserIdByUsername(username);
        return followRepository.findByFollowerIdAndFollowingId(followerId, followingId).isPresent();
    }
}
