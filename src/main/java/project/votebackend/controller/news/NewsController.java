package project.votebackend.controller.news;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.news.NewsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    //북마크 처리
    @PostMapping("/bookmark")
    @Operation(summary = "북마크 API", description = "뉴스에 북마크를 누릅니다(토글 형식).")
    public ResponseEntity<?> bookmark(
            @RequestParam Long clusterId,
            @AuthenticationPrincipal CustumUserDetails userDetails
    ) {
        newsService.bookmark(clusterId, userDetails.getId());
        return ResponseEntity.ok("success");
    }
}
