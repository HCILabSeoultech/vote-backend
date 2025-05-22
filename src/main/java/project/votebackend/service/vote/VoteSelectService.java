package project.votebackend.service.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.user.User;
import project.votebackend.domain.vote.Vote;
import project.votebackend.domain.vote.VoteOption;
import project.votebackend.domain.vote.VoteSelection;
import project.votebackend.dto.voteSelect.VoteSelectResponse;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.VoteException;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.vote.VoteOptionRepository;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.repository.vote.VoteSelectRepository;
import project.votebackend.type.ErrorCode;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteSelectService{

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteSelectRepository voteSelectRepository;

    //투표 참여
    @Transactional
    public VoteSelectResponse saveVoteSelection(Long userId, Long voteId, Long optionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));
        VoteOption option = voteOptionRepository.findById(optionId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_OPTION_NOT_FOUND));

        //마감시간이 지났으면 투표 불가
        if (LocalDateTime.now().isAfter(vote.getFinishTime())) {
            throw new VoteException(ErrorCode.VOTE_ALREADY_FINISHED);
        }

        // 기존 선택이 있으면 수정
        Optional<VoteSelection> existing = voteSelectRepository.findByUserAndVote(user, vote);
        VoteSelection selection = existing.orElse(new VoteSelection());

        selection.setUser(user);
        selection.setVote(vote);
        selection.setOption(option);

        voteSelectRepository.save(selection);

        return VoteSelectResponse.builder()
                .voteId(voteId)
                .optionId(optionId)
                .optionContent(option.getOption())
                .userId(userId)
                .build();
    }

    //투표 취소
    @Transactional
    public void cancelVoteSelection(Long userId, Long voteId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));

        // 마감된 투표는 취소 불가
        if (LocalDateTime.now().isAfter(vote.getFinishTime())) {
            throw new VoteException(ErrorCode.VOTE_ALREADY_FINISHED);
        }

        VoteSelection selection = voteSelectRepository.findByUserAndVote(user, vote)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_SELECTION_NOT_FOUND));

        voteSelectRepository.delete(selection);
    }
}

