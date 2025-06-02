package project.votebackend.service.user;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.category.Category;
import project.votebackend.domain.user.User;
import project.votebackend.domain.user.UserInterest;
import project.votebackend.domain.vote.Vote;
import project.votebackend.dto.user.UserResponseDto;
import project.votebackend.dto.user.UserUpdateDto;
import project.votebackend.dto.vote.LoadVoteDto;
import project.votebackend.dto.user.UserPageDto;
import project.votebackend.elasticSearch.UserDocument;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.CategoryException;
import project.votebackend.repository.category.CategoryRepository;
import project.votebackend.repository.follow.FollowRepository;
import project.votebackend.repository.user.UserInterestRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.repository.vote.VoteSelectRepository;
import project.votebackend.service.file.FileManagingService;
import project.votebackend.type.ErrorCode;
import project.votebackend.util.VoteStatisticsUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final FollowRepository followRepository;
    private final VoteSelectRepository voteSelectRepository;
    private final VoteStatisticsUtil voteStatisticsUtil;
    private final UserInterestRepository userInterestRepository;
    private final CategoryRepository categoryRepository;
    private final FileManagingService fileManagingService;
    private final ElasticsearchClient elasticsearchClient;

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
        Long participatedCount = voteSelectRepository.countByUserId(userId);
        Long followerCount = followRepository.countByFollowing(user);
        Long followingCount = followRepository.countByFollower(user);

        // 7. DTO 조립 및 반환
        return UserPageDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .introduction(user.getIntroduction())
                .point(user.getPoint())
                .posts(voteDto)
                .postCount(postCount)
                .participatedCount(participatedCount)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .createdAt(user.getCreatedAt())
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
        Long participatedCount = voteSelectRepository.countByUserId(userId);
        Long followerCount = followRepository.countByFollowing(user);
        Long followingCount = followRepository.countByFollower(user);

        // 6. 사용자 페이지 DTO 반환
        return UserPageDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .introduction(user.getIntroduction())
                .point(user.getPoint())
                .posts(voteDto)
                .postCount(postCount)
                .participatedCount(participatedCount)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .createdAt(user.getCreatedAt())
                .build();
    }

    //회원정보 수정
    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // 프로필 이미지 변경
        if (dto.getProfileImage() != null) {
            String newImage = dto.getProfileImage();
            String oldImage = user.getProfileImage();

            if (!Objects.equals(oldImage, "default.png") && !Objects.equals(newImage, oldImage)) {
                fileManagingService.deleteImage(oldImage);  // 기존 이미지 삭제
            }

            user.setProfileImage(newImage);
        }

        // 이름, 소개 변경
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getIntroduction() != null) user.setIntroduction(dto.getIntroduction());

        // 관심 카테고리 변경
        if (dto.getInterestCategory() != null) {
            userInterestRepository.deleteByUser(user);

            for (Long categoryId : dto.getInterestCategory()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

                UserInterest interest = UserInterest.builder()
                        .user(user)
                        .category(category)
                        .build();

                userInterestRepository.save(interest);
            }
        }

        // 관심 카테고리 이름 목록 재조회
        List<String> interestCategoryNames = userInterestRepository.findByUser(user).stream()
                .map(ui -> ui.getCategory().getName())
                .toList();

        // Elasticsearch 업데이트
        try {
            elasticsearchClient.delete(d -> d.index("users").id(String.valueOf(user.getUserId())));
            elasticsearchClient.index(i -> i
                    .index("users")
                    .id(String.valueOf(user.getUserId()))
                    .document(UserDocument.fromEntity(user)));
        } catch (IOException e) {
            log.error("Elasticsearch 업데이트 실패", e);
        }

        return UserResponseDto.fromEntity(user, interestCategoryNames);
    }
}
