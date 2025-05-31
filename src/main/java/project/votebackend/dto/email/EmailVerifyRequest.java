package project.votebackend.dto.email;

import lombok.Data;

@Data
public class EmailVerifyRequest {
    private String email;
    private String code;
}
