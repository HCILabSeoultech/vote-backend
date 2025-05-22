package project.votebackend.service.reaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.reaction.Reaction;
import project.votebackend.domain.user.User;
import project.votebackend.domain.vote.Vote;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.VoteException;
import project.votebackend.repository.reaction.ReactionRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.type.ErrorCode;
import project.votebackend.type.ReactionType;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    //좋아요 처리(토글)
    @Transactional
    public void like(Long voteId, Long userId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        reactionRepository.findByUserAndVoteAndReaction(user, vote, ReactionType.LIKE)
                .ifPresentOrElse(
                        reactionRepository::delete,
                        () -> {
                            Reaction reaction = Reaction.builder()
                                    .vote(vote)
                                    .user(user)
                                    .reaction(ReactionType.LIKE)
                                    .build();
                            reactionRepository.save(reaction);
                        }
                );
    }

    //북마크 처리(토글)
    @Transactional
    public void bookmark(Long voteId, Long userId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        reactionRepository.findByUserAndVoteAndReaction(user, vote, ReactionType.BOOKMARK)
                .ifPresentOrElse(
                        reactionRepository::delete,
                        () -> {
                            Reaction reaction = Reaction.builder()
                                    .vote(vote)
                                    .user(user)
                                    .reaction(ReactionType.BOOKMARK)
                                    .build();
                            reactionRepository.save(reaction);
                        }
                );
    }
}
