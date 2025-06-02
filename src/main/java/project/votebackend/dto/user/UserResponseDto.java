package project.votebackend.dto.user;

import lombok.Builder;
import lombok.Getter;
import project.votebackend.domain.user.User;

import java.util.List;

@Getter
@Builder
public class UserResponseDto {
    private Long userId;
    private String name;
    private String profileImage;
    private String introduction;
    private List<String> interestCategories;

    public static UserResponseDto fromEntity(User user, List<String> interestCategories) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .introduction(user.getIntroduction())
                .interestCategories(interestCategories)
                .build();
    }
}
