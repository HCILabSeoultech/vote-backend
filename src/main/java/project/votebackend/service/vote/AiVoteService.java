package project.votebackend.service.vote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.category.Category;
import project.votebackend.domain.user.User;
import project.votebackend.domain.vote.Vote;
import project.votebackend.domain.vote.VoteImage;
import project.votebackend.domain.vote.VoteOption;
import project.votebackend.dto.vote.AiBatchResult;
import project.votebackend.dto.vote.CreateVoteRequest;
import project.votebackend.dto.vote.VoteOptionDto;
import project.votebackend.repository.category.CategoryRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.type.VoteStatus;
import project.votebackend.type.VoteType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiVoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Value("${vote.ai.user-id}")
    private Long aiUserId;

    //전체 배치 처리
    @Transactional
    public List<AiBatchResult> uploadBatch(List<CreateVoteRequest> requests) {
        List<AiBatchResult> results = new ArrayList<>();
        int idx = 0;
        for (CreateVoteRequest req : requests) {
            try {
                Long id = saveOneInNewTx(req);
                results.add(new AiBatchResult(idx, req.getTitle(), true, id, null));
            } catch (Exception e) {
                log.warn("[AI-VOTE] fail idx={} title={} cause={}", idx, req.getTitle(), e.getMessage());
                results.add(new AiBatchResult(idx, req.getTitle(), false, null, e.getMessage()));
            }
            idx++;
        }
        return results;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long saveOneInNewTx(CreateVoteRequest req) {
        // 기본 검증
        if (req.getCategoryId() == null) {
            throw new IllegalArgumentException("categoryId is required");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        List<VoteOptionDto> optionDtos = req.getOptions();
        if (optionDtos == null || optionDtos.isEmpty()) {
            throw new IllegalArgumentException("options must not be empty");
        }

        // 사용자 (AI 시스템 계정)
        User aiUser = userRepository.findById(aiUserId)
                .orElseThrow(() -> new IllegalStateException("AI user not found: " + aiUserId));

        // 카테고리
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid categoryId: " + req.getCategoryId()));

        // voteType
        final VoteType voteType;
        try {
            voteType = VoteType.valueOf(req.getVoteType().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid voteType. Allowed values: TEXT, IMAGE, VIDEO");
        }

        // content가 URL이면 link로 승격
        String content = req.getContent();

        // Vote 생성
        Vote vote = Vote.builder()
                .title(req.getTitle())
                .content(content)
                .finishTime(req.getFinishTime())
                .status(VoteStatus.PUBLISHED)
                .voteType(voteType)
                .createdByAI(true)
                .build();
        vote.setUser(aiUser);
        vote.setCategory(category);

        // 옵션
        if (vote.getOptions() == null) vote.setOptions(new HashSet<>());
        for (VoteOptionDto od : optionDtos) {
            VoteOption o = new VoteOption();
            o.setVote(vote);
            o.setOption(od.getContent());
            o.setOptionImage(od.getOptionImage());
            vote.getOptions().add(o);
        }

        // 이미지
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            if (vote.getImages() == null) vote.setImages(new HashSet<>());
            req.getImageUrls().forEach(url -> {
                VoteImage img = new VoteImage();
                img.setVote(vote);
                img.setImageUrl(url);
                vote.getImages().add(img);
            });
        }

        // 저장
        Vote saved = voteRepository.save(vote);
        return saved.getVoteId();
    }
}
