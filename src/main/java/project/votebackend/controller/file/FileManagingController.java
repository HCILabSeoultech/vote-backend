package project.votebackend.controller.file;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.votebackend.service.file.FileManagingService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FileManagingController {

    private final FileManagingService fileManagingService;

    //이미지 업로드
    @PostMapping("/image/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileManagingService.storeFile(file);
        return ResponseEntity.ok(imageUrl);
    }

    //이미지 삭제
    @DeleteMapping("/image/delete")
    public ResponseEntity<?> deleteImage(@RequestBody Map<String, String> body) {
        String fileUrl = body.get("fileUrl");
        fileManagingService.deleteImage(fileUrl);
        return ResponseEntity.ok("삭제 완료");
    }

    //영상 업로드
    @PostMapping("/video/upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        String videoUrl = fileManagingService.storeVideo(file);
        return ResponseEntity.ok(videoUrl);
    }

    //영상 삭제
    @DeleteMapping("/video/delete")
    public ResponseEntity<?> deleteVideo(@RequestBody Map<String, String> body) {
        String fileUrl = body.get("fileUrl");
        fileManagingService.deleteVideo(fileUrl);
        return ResponseEntity.ok("삭제 완료");
    }
}
