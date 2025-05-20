package project.votebackend.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FollowUserDto {
    private Long userId;
    private String username;
    private String profileImage;
}
