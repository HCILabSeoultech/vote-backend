package project.votebackend.dto.login;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RegisterRequest {
    private String token;
    private String os;
}
