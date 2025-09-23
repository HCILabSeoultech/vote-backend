package project.votebackend.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiBatchResult {
    private int index;         // 요청 배열 내 인덱스
    private String title;      // 생성 시도한 제목
    private boolean success;   // 성공 여부
    private Long voteId;       // 성공 시 생성된 ID
    private String error;      // 실패 시 사유
}
