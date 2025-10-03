package project.votebackend.dto.user;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import project.votebackend.dto.vote.LoadVoteDto;
import project.votebackend.type.Grade;

import java.time.LocalDateTime;

@Data
@Builder
public class UserPageDto {

    private String name;
    private String profileImage;
    private String address;
    private Long followerCount;
    private Long followingCount;
    private MypageStat mypageStat;
    private LevelInfo levelInfo;
    private LocalDateTime createdAt;
}
