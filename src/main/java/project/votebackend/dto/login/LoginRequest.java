package project.votebackend.dto.login;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String username;
    private String password;
}
