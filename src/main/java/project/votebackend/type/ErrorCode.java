package project.votebackend.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //Auth
    ALREADY_EXIST_NAME(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    ALREADY_EXIST_PHONE(HttpStatus.CONFLICT, "이미 존재하는 전화번호입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"내부 서버 오류 발생"),
    USERNAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 아이디입니다."),
    PASSWORD_NOT_MATCHED(HttpStatus.CONFLICT, "비밀번호가 일치하지 않습니다."),
    USER_NOT_MATCHED(HttpStatus.CONFLICT, "유저가 일치하지 않습니다."),

    //Category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),

    //Vote
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 투표입니다."),
    VOTE_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 투표옵션입니다."),
    VOTE_ALREADY_FINISHED(HttpStatus.CONFLICT, "이미 종료된 투표입니다."),
    VOTE_SELECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "참여하지 않은 투표입니다."),

    //Comment
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부모 댓글이 존재하지 않습니다."),
    COMMENT_NOT_FOUNT(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),

    //Follow
    ALREADY_FOLLOW(HttpStatus.CONFLICT, "이미 팔로우 중입니다."),

    //Mail
    CODE_NOT_MATCHED(HttpStatus.CONFLICT, "코드가 일치하지 않습니다.");


    private final HttpStatus httpStatus;
    private final String description;
}
