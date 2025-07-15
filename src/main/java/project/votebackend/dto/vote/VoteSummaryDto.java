package project.votebackend.dto.vote;

import lombok.*;
import project.votebackend.domain.vote.Vote;
import project.votebackend.domain.vote.VoteImage;
import project.votebackend.domain.vote.VoteOption;
import project.votebackend.type.ReactionType;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteSummaryDto {
    private Long voteId;
    private String title;
    private String thumbnailImageUrl;

    private int totalVotes;              // 누적 투표 수
//    private int todayVotes;              // 오늘 투표 수
    private int commentCount;            // 댓글 수
    private int likeCount;               // 좋아요 수
    private LocalDateTime finishTime;

//    private int rankTotal;               // 누적 투표 기준 랭킹
//    private int rankToday;               // 오늘 득표 기준 랭킹
//    private int rankComment;             // 댓글 기준 랭킹

//    private int rankChangeTotal;         // 누적 득표 순위 변화
//    private int rankChangeToday;         // 오늘 득표 순위 변화
//    private int rankChangeComment;

//    private int ongoingCommentRank;         // 진행중인 투표 댓글 수 랭킹
//    private int ongoingVoteCountRank;       // 진행중인 투표 투표 수 랭킹
//
//    private int ongoingCommentRankChange;   // 진행중인 투표 댓글 수 랭킹 변화
//    private int ongoingVoteCountRankChange; // 진행중인 투표 투표 수 랭킹 변화
//
//    private int endedCommentRank;         // 종료된 투표 댓글 수 랭킹
//    private int endedVoteCountRank;       // 종료된 투표 투표 수 랭킹
//
//    private int endedCommentRankChange;   // 종료된 투표 댓글 수 랭킹 변화
//    private int endedVoteCountRankChange; // 종료된 투표 투표 수 랭킹 변화

    // 투표에 등록된 첫 이미지 썸네일 반환
    private static String extractThumbnail(Vote vote) {
        Optional<String> imageUrl = vote.getImages().stream()
                .findFirst()
                .map(VoteImage::getImageUrl); // Optional<String>

        if (imageUrl.isPresent()) {
            return imageUrl.get();
        }

        // 이미지가 없다면 옵션 이미지 중 첫 번째
        return vote.getOptions().stream()
                .map(VoteOption::getOptionImage)
                .filter(img -> img != null && !img.isEmpty())
                .findFirst()
                .orElse(null); // 또는 기본 이미지 URL
    }

    public static VoteSummaryDto from(Vote vote) {
        return VoteSummaryDto.builder()
                .voteId(vote.getVoteId())
                .title(vote.getTitle())
                .finishTime(vote.getFinishTime())
                .thumbnailImageUrl(VoteSummaryDto.extractThumbnail(vote))
                .totalVotes(vote.getSelections().size())
                .commentCount((int) vote.getComments().stream().filter(c -> c.getParent() == null).count())
                .likeCount((int) vote.getReactions().stream()
                        .filter(r -> r.getReaction() == ReactionType.LIKE)
                        .count())
                .build();
    }
}
