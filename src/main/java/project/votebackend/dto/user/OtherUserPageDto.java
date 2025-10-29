package project.votebackend.dto.user;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import project.votebackend.dto.vote.VoteSummaryDto;
import java.time.LocalDateTime;

@Data
@Builder
public class OtherUserPageDto {

    private String name;
    private String profileImage;
    private Long followerCount;
    private Long followingCount;
    private MypageStat mypageStat;
    private Long postCount;
    private LevelInfo levelInfo;
    private LocalDateTime createdAt;
    private Page<VoteSummaryDto> posts;
}
