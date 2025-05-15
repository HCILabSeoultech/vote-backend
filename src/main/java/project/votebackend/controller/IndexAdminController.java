package project.votebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.service.IndexAdminService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class IndexAdminController {

    private final IndexAdminService indexAdminService;

    //Elasticsearch 투표 인덱싱
    @PostMapping("/create-votes-index")
    public ResponseEntity<String> createVotesIndex() {
        try {
            indexAdminService.createVotesIndex();
            return ResponseEntity.ok("votes 인덱스 생성 성공!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("인덱스 생성 실패: " + e.getMessage());
        }
    }

    //Elasticsearch 유저 인덱싱
    @PostMapping("/create-users-index")
    public ResponseEntity<String> createUsersIndex() {
        try {
            indexAdminService.createUsersIndex();
            return ResponseEntity.ok("users 인덱스 생성 성공!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("users 인덱스 생성 실패: " + e.getMessage());
        }
    }
}
