package project.votebackend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LevelInfo {
    private String currentLevel;  // 현재 등급
    private String nextLevel;     // 다음 등급 (없으면 null)
}
