package project.votebackend.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.comment.Comment;
import project.votebackend.domain.user.User;
import project.votebackend.domain.vote.Vote;
import project.votebackend.dto.comment.CommentResponse;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.CommentException;
import project.votebackend.exception.VoteException;
import project.votebackend.repository.comment.CommentRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.type.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    @Transactional
    public CommentResponse createComment(Long voteId, String content, String username, Long parentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setVote(vote);
        comment.setContent(content);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new CommentException(ErrorCode.PARENT_COMMENT_NOT_FOUND));
            comment.setParent(parent);
        }

        commentRepository.save(comment);
        return CommentResponse.fromEntity(comment, user.getUserId());
    }

    // 댓글 조회
    public Page<CommentResponse> getComments(Long voteId, String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")); // 오래된 순 정렬

        // 부모 댓글만 페이징
        Page<Comment> parentComments = commentRepository.findByVote_VoteIdAndParentIsNull(voteId, pageable);

        // 각 부모 댓글에 대댓글 붙이기
        List<CommentResponse> commentResponses = parentComments.getContent().stream()
                .map(parent -> {
                    // 대댓글 전체 조회
                    List<Comment> replies = commentRepository.findByParent_CommentIdOrderByCreatedAtAsc(parent.getCommentId());

                    return CommentResponse.fromEntityWithReplies(parent, replies, user.getUserId());
                })
                .collect(Collectors.toList());

        // Page 객체로 감싸기
        return new PageImpl<>(commentResponses, pageable, parentComments.getTotalElements());
    }

    //댓글 수정
    @Transactional
    public CommentResponse editComment(Long commentId, String content, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUNT));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new AuthException(ErrorCode.USER_NOT_MATCHED);
        }

        comment.setContent(content);
        return CommentResponse.fromEntity(comment, comment.getUser().getUserId());
    }

    //댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUNT));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new AuthException(ErrorCode.USER_NOT_MATCHED);
        }

        commentRepository.delete(comment);
    }
}
