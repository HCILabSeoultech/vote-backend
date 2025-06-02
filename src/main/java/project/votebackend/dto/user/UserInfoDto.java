package project.votebackend.dto.user;

import lombok.Builder;
import lombok.Data;
import project.votebackend.domain.category.Category;
import project.votebackend.type.Gender;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UserInfoDto {
    private String username;
    private String name;
    private String profileImage;
    private Gender gender;
    private LocalDate birthdate;
    private String address;
    private String phone;
    private List<Long> userInterests;
    private String introduction;
}
