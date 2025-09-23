package project.votebackend.controller.vote;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.dto.vote.AiBatchResult;
import project.votebackend.dto.vote.CreateVoteRequest;
import project.votebackend.service.vote.AiVoteService;

import java.util.List;

@RestController
@RequestMapping("/ai/votes")
@RequiredArgsConstructor
public class AiVoteController {

    private final AiVoteService aiVoteService;

    @PostMapping
    @Operation(summary = "AI 투표 일괄 저장(호출 바디만 저장)", description = "FastAPI 호출 없이, 전달받은 JSON 배열을 그대로 DB에 저장합니다.")
    public ResponseEntity<List<AiBatchResult>> saveBatch(@RequestBody List<CreateVoteRequest> requests) {
        List<AiBatchResult> ai = aiVoteService.uploadBatch(requests);
        return ResponseEntity.ok(ai);
    }
}
