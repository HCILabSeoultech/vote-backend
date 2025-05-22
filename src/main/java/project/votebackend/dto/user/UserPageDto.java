package project.votebackend.dto.user;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import project.votebackend.dto.vote.LoadVoteDto;

@Data
@Builder
public class UserPageDto {

    private String username;
    private String name;
    private String profileImage;
    private String introduction;
    private Long point;
    private Long followerCount;
    private Long followingCount;
    private Long postCount;
    private Page<LoadVoteDto> posts;


}
