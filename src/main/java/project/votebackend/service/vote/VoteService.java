package project.votebackend.service.vote;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.category.Category;
import project.votebackend.domain.user.User;
import project.votebackend.domain.vote.*;
import project.votebackend.dto.vote.CreateVoteRequest;
import project.votebackend.elasticSearch.VoteDocument;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.CategoryException;
import project.votebackend.exception.VoteException;
import project.votebackend.repository.category.CategoryRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.vote.VoteImageRepository;
import project.votebackend.repository.vote.VoteOptionRepository;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.repository.voteStat.VoteStat6hRepository;
import project.votebackend.repository.voteStat.VoteStatHourlyRepository;
import project.votebackend.type.ErrorCode;
import project.votebackend.type.VoteStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final VoteImageRepository voteImageRepository;
    private final VoteStat6hRepository voteStat6hRepository;
    private final VoteStatHourlyRepository voteStatHourlyRepository;

    //투표 생성
    @Transactional
    public Vote createVote(CreateVoteRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

        Vote vote = Vote.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .finishTime(request.getFinishTime())
                .status(VoteStatus.DRAFT)
                .build();

        vote.setUser(user);
        vote.setCategory(category);


        // 옵션 추가
        Set<VoteOption> options = request.getOptions().stream().map(opt -> {
            VoteOption o = new VoteOption();
            o.setVote(vote);
            o.setOption(opt.getContent());
            o.setOptionImage(opt.getOptionImage());
            return o;
        }).collect(Collectors.toSet());

        vote.setOptions(options);

        // 이미지 추가
        if (request.getImageUrls() != null) {
            Set<VoteImage> images = request.getImageUrls().stream().map(url -> {
                VoteImage img = new VoteImage();
                img.setVote(vote);
                img.setImageUrl(url);
                return img;
            }).collect(Collectors.toSet());
            vote.setImages(images);
        }

        Vote savedVote = voteRepository.save(vote);

        //Elasticsearch에 저장
        try {
            VoteDocument doc = VoteDocument.fromEntity(savedVote);
            elasticsearchClient.index(i -> i
                    .index("votes")
                    .id(String.valueOf(doc.getId()))
                    .document(doc)
            );
        } catch (IOException e) {
            log.error("Elasticsearch 저장 실패", e);
        }

        return savedVote;
    }

    // 투표 업로드
    @Transactional
    public void publishVote(Long voteId, Long userId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));

        if (!vote.getUser().getUserId().equals(userId)) {
            throw new AuthException(ErrorCode.USER_NOT_MATCHED);
        }

        vote.setStatus(VoteStatus.PUBLISHED);

        // 게시할 때 Elasticsearch 저장
        try {
            VoteDocument doc = VoteDocument.fromEntity(vote);
            elasticsearchClient.index(i -> i
                    .index("votes")
                    .id(String.valueOf(doc.getId()))
                    .document(doc)
            );
        } catch (IOException e) {
            log.error("Elasticsearch 저장 실패", e);
        }
    }

    // 투표 재업로드
    @Transactional
    public Long reuploadVote(Long originalVoteId, LocalDateTime newFinishTime, String username) {
        Vote original = voteRepository.findById(originalVoteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));

        if (!original.getUser().getUsername().equals(username)) {
            throw new AuthException(ErrorCode.USER_NOT_MATCHED);
        }

        // 새로운 투표 객체 생성
        Vote newVote = new Vote();
        newVote.setTitle(original.getTitle());
        newVote.setContent(original.getContent());
        newVote.setCategory(original.getCategory());
        newVote.setUser(original.getUser());
        newVote.setFinishTime(newFinishTime);
        voteRepository.save(newVote);

        // 옵션 복사
        List<VoteOption> newOptions = original.getOptions().stream()
                .map(opt -> {
                    VoteOption newOpt = new VoteOption();
                    newOpt.setVote(newVote);
                    newOpt.setOption(opt.getOption());
                    newOpt.setOptionImage(opt.getOptionImage());
                    return newOpt;
                })
                .collect(Collectors.toList());
        voteOptionRepository.saveAll(newOptions);

        // 이미지 복사
        if (original.getImages() != null) {
            List<VoteImage> newImages = original.getImages().stream()
                    .map((VoteImage img) -> new VoteImage(img.getImageUrl(), newVote))
                    .collect(Collectors.toList());
            voteImageRepository.saveAll(newImages);
        }

        //Elasticsearch에 저장
        try {
            VoteDocument doc = VoteDocument.fromEntity(newVote);
            elasticsearchClient.index(i -> i
                    .index("votes")
                    .id(String.valueOf(doc.getId()))
                    .document(doc)
            );
        } catch (IOException e) {
            log.error("Elasticsearch 저장 실패", e);
        }

        return newVote.getVoteId();
    }

    // 투표 삭제
    @Transactional
    public void deleteVote(Long voteId, String username) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));

        if (!vote.getUser().getUsername().equals(username)) {
            throw new AuthException(ErrorCode.USER_NOT_MATCHED);
        }

        voteStatHourlyRepository.deleteByVoteId(voteId);
        voteStat6hRepository.deleteByVoteId(voteId);

        voteRepository.delete(vote);

        // Elasticsearch에서도 삭제
        try {
            elasticsearchClient.delete(d -> d
                    .index("votes")
                    .id(String.valueOf(voteId))
            );
        } catch (IOException e) {
            log.error("Elasticsearch 삭제 실패", e);

        }
    }
}
