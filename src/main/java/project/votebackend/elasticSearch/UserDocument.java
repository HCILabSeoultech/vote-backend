package project.votebackend.elasticSearch;

import lombok.*;
import project.votebackend.domain.user.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocument {
    private Long id;
    private String username;
    private String profileImage;

    public static UserDocument fromEntity(User user) {
        return new UserDocument(
                user.getUserId(),
                user.getName(),
                user.getProfileImage()
        );
    }
}
