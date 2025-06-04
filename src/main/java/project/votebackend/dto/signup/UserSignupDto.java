package project.votebackend.dto.signup;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import project.votebackend.type.Gender;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserSignupDto {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$",
            message = "비밀번호는 영문 소문자, 숫자, 특수문자를 포함한 8자 이상이어야 합니다."
    )
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010-?\\d{4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phone;

    @NotNull(message = "생년월일은 필수입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthdate;

    private String address;

    @Size(max = 200, message = "자기소개는 200자 이내여야 합니다.")
    private String introduction;

    private String profileImage;

    private List<Long> interestCategory;
}
