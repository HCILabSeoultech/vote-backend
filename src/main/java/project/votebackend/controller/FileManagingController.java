package project.votebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.votebackend.service.FileManagingService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FileManagingController {

    private final FileManagingService fileStorageService;

    //이미지 업로드
    @PostMapping("/image/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileStorageService.storeFile(file);
        return ResponseEntity.ok(imageUrl);
    }

    //이미지 삭제
    @DeleteMapping("/image/delete")
    public ResponseEntity<?> deleteImage(@RequestBody Map<String, String> body) {
        String fileUrl = body.get("fileUrl");
        fileStorageService.deleteFile(fileUrl);
        return ResponseEntity.ok("삭제 완료");
    }
}
