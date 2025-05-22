package project.votebackend.controller.search;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.votebackend.service.elasticsearch.MigrationService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/migrate")
public class MigrationController {
    private final MigrationService migrationService;

    //투표 마이그레이션
    @PostMapping("/votes")
    public String migrateVotes() {
        try {
            migrationService.migrateVotesToElasticsearch();
            return "Migration success";
        } catch (IOException e) {
            return "Migration failed";
        }
    }

    //유저 마이그레이션
    @PostMapping("/users")
    public String migrateUsers() {
        try {
            migrationService.migrateUsersToElasticsearch();
            return "Migration success";
        } catch (IOException e) {
            return "Migration failed";
        }
    }
}
