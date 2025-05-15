package project.votebackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.Comment;
import project.votebackend.domain.CommentLike;
import project.votebackend.domain.User;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.CommentException;
import project.votebackend.repository.CommentLikeRepository;
import project.votebackend.repository.CommentRepository;
import project.votebackend.repository.UserRepository;
import project.votebackend.type.ErrorCode;

@Service
@RequiredArgsConstructor
public class CommentLikeService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    // 댓글 좋아요/취소 기능 (토글 방식)
    // true → 좋아요 성공, false → 좋아요 취소
    @Transactional
    public boolean like(Long commentId, String username) {
        // 1. 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // 2. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUNT));

        // 3. 이미 좋아요한 기록이 있는지 확인
        return commentLikeRepository.findByUserAndComment(user, comment)
                .map(existing -> {
                    // 이미 좋아요를 누른 상태라면: 좋아요 취소
                    commentLikeRepository.delete(existing);                     // 좋아요 기록 삭제
                    comment.setLikeCount(comment.getLikeCount() - 1);          // 좋아요 수 감소
                    return false;                                               // 좋아요 해제됨
                })
                .orElseGet(() -> {
                    // 좋아요하지 않은 상태라면: 좋아요 추가
                    CommentLike like = new CommentLike();                       // 새 좋아요 객체 생성
                    like.setUser(user);                                         // 사용자 설정
                    like.setComment(comment);                                   // 댓글 설정
                    commentLikeRepository.save(like);                           // 좋아요 저장
                    comment.setLikeCount(comment.getLikeCount() + 1);          // 좋아요 수 증가
                    return true;                                                // 좋아요 성공
                });
    }

    //좋아요 수 계산
    public long getLikeCount(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUNT));
        return comment.getLikeCount();
    }
}
