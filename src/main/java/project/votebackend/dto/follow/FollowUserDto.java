package project.votebackend.dto.follow;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FollowUserDto {
    private Long userId;
    private String name;
    private String profileImage;
    private boolean isFollowing;
}
