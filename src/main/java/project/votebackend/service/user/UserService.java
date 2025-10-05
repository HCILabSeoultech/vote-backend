package project.votebackend.service.user;

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
import project.votebackend.dto.user.*;
import project.votebackend.dto.vote.OtherUserVotes;
import project.votebackend.dto.vote.VoteSummaryDto;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.CategoryException;
import project.votebackend.repository.category.CategoryRepository;
import project.votebackend.repository.follow.FollowRepository;
import project.votebackend.repository.user.UserInterestRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.user.UserStatDao;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.repository.vote.VoteSelectRepository;
import project.votebackend.type.ErrorCode;
import project.votebackend.type.VoteStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final FollowRepository followRepository;
    private final VoteSelectRepository voteSelectRepository;
    private final UserInterestRepository userInterestRepository;
    private final CategoryRepository categoryRepository;
    private final UserStatDao userStatDao;

    // [마이페이지 조회] - 로그인한 본인의 정보를 조회
    public UserPageDto getMyPage(Long userId) {
        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // 1) 월별 6달
        List<Object[]> rows = userStatDao.findMonthlyReceivedVotes6(userId);
        List<MonthlyStat> monthly = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            String ymStr = (String) r[0];                 // "YYYY-MM"
            long cnt     = ((Number) r[1]).longValue();
            YearMonth ym = YearMonth.parse(ymStr);        // 바로 파싱
            monthly.add(new MonthlyStat(ym, cnt));
        }

        // 2) 총합/개월수
        long total  = userStatDao.findTotalReceivedVotes(userId);
        int months  = userStatDao.findMonthsSinceSignup(userId);

        // 3) 이번 달 받은 투표 수
        long currentMonth = userStatDao.findCurrentMonthReceivedVotes(userId);

        // 4) 이번 달 제외 지표 계산
        long totalExclThis = total - currentMonth;
        int monthsExclThis = Math.max(months - 1, 0);

        BigDecimal avgExcl = monthsExclThis > 0
                ? BigDecimal.valueOf(totalExclThis)
                .divide(BigDecimal.valueOf(monthsExclThis), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 5) 등급 계산
        LevelInfo levelInfo = mapToLevel(avgExcl);

        MypageStat mypageStat = MypageStat.builder()
                .monthly(monthly)
                .total(total)
                .months(months)
                .build();

        Long followerCount = followRepository.countByFollowing(user);
        Long followingCount = followRepository.countByFollower(user);

        // 3. DTO 조립 및 반환
        return UserPageDto.builder()
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .address(user.getAddress())
                .followerCount(followerCount)
                .followingCount(followingCount)
                .mypageStat(mypageStat)
                .levelInfo(levelInfo)
                .createdAt(user.getCreatedAt())
                .build();
    }

    // [다른 유저 페이지 조회] - userId 기준으로 프로필과 게시글을 조회
    public OtherUserPageDto getUserPage(Long userId, Pageable pageable) {
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
        Page<Vote> votes = voteRepository.findByUser_UserIdAndStatus(userId, VoteStatus.PUBLISHED, sortedPageable);

        // 4. DTO 변환
        Page<VoteSummaryDto> voteDto = OtherUserVotes.otherUserVotes(votes, sortedPageable);

        // 5. 게시글 수, 팔로워 수, 팔로잉 수 계산
        Long postCount = voteRepository.countByUser_UserId(userId);
        Long followerCount = followRepository.countByFollowing(user);
        Long followingCount = followRepository.countByFollower(user);

        // 6. 사용자 페이지 DTO 반환
        return OtherUserPageDto.builder()
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .address(user.getAddress())
                .posts(voteDto)
                .postCount(postCount)
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

        // 이름, 소개, 이미지 변경
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getIntroduction() != null) user.setIntroduction(dto.getIntroduction());
        user.setProfileImage(dto.getProfileImage());

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

        return UserResponseDto.fromEntity(user, interestCategoryNames);
    }

    //유저정보 조회
    public UserInfoDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // 관심사 카테고리 ID만 추출
        List<Long> interestIds = user.getUserInterests().stream()
                .map(userInterest -> userInterest.getCategory().getCategoryId())
                .toList();


        return UserInfoDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .gender(user.getGender())
                .profileImage(user.getProfileImage())
                .birthdate(user.getBirthdate())
                .address(user.getAddress())
                .phone(user.getPhone())
                .userInterests(interestIds)
                .introduction(user.getIntroduction())
                .build();
    }

    public boolean getDraftHelpVersionSeen(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        return user.isDraftHelpVersionSeen();
    }

    private LevelInfo mapToLevel(BigDecimal avgPerMonthExcl) {
        int v = avgPerMonthExcl.intValue();

        if (v >= 100_000) {
            return new LevelInfo("Master", null); // 최고 등급이면 다음 없음
        }
        else if (v >= 10_000) {
            return new LevelInfo("Diamond", "Master");
        }
        else if (v >= 1_000) {
            return new LevelInfo("Platinum", "Diamond");
        }
        else if (v >= 500) {
            return new LevelInfo("Gold", "Platinum");
        }
        else if (v >= 200) {
            return new LevelInfo("Silver", "Gold");
        }
        else {
            return new LevelInfo("Bronze", "Silver");
        }
    }
}
