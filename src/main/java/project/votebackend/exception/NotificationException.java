package project.votebackend.exception;

import lombok.*;
import project.votebackend.type.ErrorCode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationException extends RuntimeException {

  private ErrorCode errorCode;
  private String errorMessage;

  public NotificationException(ErrorCode errorCode){
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }

  public int getHttpStatus() {
    return errorCode.getHttpStatus().value();
  }
}
