package project.votebackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import project.votebackend.domain.User;
import project.votebackend.domain.Vote;
import project.votebackend.dto.LoadVoteDto;
import project.votebackend.dto.UserPageDto;
import project.votebackend.exception.AuthException;
import project.votebackend.repository.FollowRepository;
import project.votebackend.repository.UserRepository;
import project.votebackend.repository.VoteRepository;
import project.votebackend.repository.VoteSelectRepository;
import project.votebackend.type.ErrorCode;
import project.votebackend.util.VoteStatisticsUtil;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final FollowRepository followRepository;
    private final VoteStatisticsUtil voteStatisticsUtil;

    // [마이페이지 조회] - 로그인한 본인의 정보를 조회
    public UserPageDto getMyPage(Long userId, Pageable pageable) {
        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // 2. 최신 순으로 정렬된 페이징 객체 생성
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 3. 사용자가 작성한 투표글 페이징 조회
        Page<Vote> votes = voteRepository.findByUser_UserId(userId, sortedPageable);

        // 4. 통계 수집용 voteId 리스트 추출
        List<Long> voteIds = votes.getContent().stream()
                .map(Vote::getVoteId)
                .toList();

        // 5. 투표 통계 수집 및 DTO 변환
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, voteIds);
        Page<LoadVoteDto> voteDto = voteStatisticsUtil.getLoadVoteDtos(userId, votes, stats, sortedPageable);

        // 6. 게시글 수, 팔로워 수, 팔로잉 수 조회
        Long postCount = voteRepository.countByUser_UserId(userId);
        Long followerCount = followRepository.countByFollowingId(userId);
        Long followingCount = followRepository.countByFollowerId(userId);

        // 7. DTO 조립 및 반환
        return UserPageDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .introduction(user.getIntroduction())
                .point(user.getPoint())
                .posts(voteDto)
                .postCount(postCount)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

    // [다른 유저 페이지 조회] - userId 기준으로 프로필과 게시글을 조회
    public UserPageDto getUserPage(Long userId, Pageable pageable) {
        // 1. 대상 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // 2. 최신순 정렬된 페이징 객체 생성
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 3. 해당 사용자의 투표글 조회
        Page<Vote> votes = voteRepository.findByUser_UserId(userId, sortedPageable);
        List<Long> voteIds = votes.getContent().stream()
                .map(Vote::getVoteId)
                .toList();

        // 4. 통계 수집 및 DTO 변환
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, voteIds);
        Page<LoadVoteDto> voteDto = voteStatisticsUtil.getLoadVoteDtos(userId, votes, stats, sortedPageable);

        // 5. 게시글 수, 팔로워 수, 팔로잉 수 계산
        Long postCount = voteRepository.countByUser_UserId(userId);
        Long followerCount = followRepository.countByFollowingId(userId);
        Long followingCount = followRepository.countByFollowerId(userId);

        // 6. 사용자 페이지 DTO 반환
        return UserPageDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .introduction(user.getIntroduction())
                .point(user.getPoint())
                .posts(voteDto)
                .postCount(postCount)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

}
