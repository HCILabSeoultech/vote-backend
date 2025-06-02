package project.votebackend.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateDto {
    private String name;
    private String profileImage; // 새 프로필 이미지 URL
    private List<Long> interestCategory;

    @Size(max = 200, message = "자기소개는 200자 이내여야 합니다.")
    private String introduction;
}
