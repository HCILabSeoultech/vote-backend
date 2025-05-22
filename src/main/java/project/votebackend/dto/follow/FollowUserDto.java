package project.votebackend.dto.follow;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FollowUserDto {
    private Long userId;
    private String username;
    private String profileImage;
}
