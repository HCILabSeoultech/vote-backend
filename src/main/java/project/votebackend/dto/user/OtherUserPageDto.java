package project.votebackend.dto.user;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import project.votebackend.dto.vote.LoadVoteDto;
import project.votebackend.dto.vote.OtherUserVotes;
import project.votebackend.type.Grade;

import java.time.LocalDateTime;

@Data
@Builder
public class OtherUserPageDto {

    private String name;
    private String profileImage;
    private String address;
    private Long avgParticipantCount;
    private Long followerCount;
    private Long followingCount;
    private Long postCount;
    private LocalDateTime createdAt;
    private Page<OtherUserVotes> posts;
}
