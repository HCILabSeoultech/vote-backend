package project.votebackend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.votebackend.dto.ErrorResponse;

import static project.votebackend.type.ErrorCode.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // AuthException 예외 처리 - 인증 관련 예외 발생 시 실행됨
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException e) {
        log.error("{}", e.getErrorCode()); // 로그에 에러 코드 출력

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus()) // 예외에 지정된 HTTP 상태 코드 반환
                .body(new ErrorResponse(e.getErrorCode(), e.getErrorMessage())); // 에러 응답 본문 생성
    }

    // CategoryException 예외 처리 - 카테고리 관련 예외 발생 시 실행됨
    @ExceptionHandler(CategoryException.class)
    public ResponseEntity<ErrorResponse> handleCategoryException(CategoryException e) {
        log.error("{}", e.getErrorCode());

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(new ErrorResponse(e.getErrorCode(), e.getErrorMessage()));
    }

    // VoteException 예외 처리 - 투표 관련 예외 발생 시 실행됨
    @ExceptionHandler(VoteException.class)
    public ResponseEntity<ErrorResponse> handleVoteException(VoteException e) {
        log.error("{}", e.getErrorCode());

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(new ErrorResponse(e.getErrorCode(), e.getErrorMessage()));
    }

    // CommentException 예외 처리 - 댓글 관련 예외 발생 시 실행됨
    @ExceptionHandler(CommentException.class)
    public ResponseEntity<ErrorResponse> handleCommentException(CommentException e) {
        log.error("{}", e.getErrorCode());

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(new ErrorResponse(e.getErrorCode(), e.getErrorMessage()));
    }

    // FollowException 예외 처리 - 팔로우 관련 예외 발생 시 실행됨
    @ExceptionHandler(FollowException.class)
    public ResponseEntity<ErrorResponse> handleFollowException(FollowException e) {
        log.error("{}", e.getErrorCode());

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(new ErrorResponse(e.getErrorCode(), e.getErrorMessage()));
    }

    // 기타 모든 예외 처리 - 명시되지 않은 예외 발생 시 실행됨
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("{}", e.getMessage()); // 예외 메시지를 로그로 출력

        // 내부 서버 오류(500)로 응답
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(new ErrorResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR.getDescription()));
    }

}
