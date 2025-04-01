package project.votebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.votebackend.domain.VoteOption;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoteOptionDto {
    private Long id;
    private String content;
    private int voteCount;

    public static VoteOptionDto fromEntity(VoteOption option, int voteCount) {
        return VoteOptionDto.builder()
                .id(option.getOptionId())
                .content(option.getOption())
                .voteCount(voteCount)
                .build();
    }
}
