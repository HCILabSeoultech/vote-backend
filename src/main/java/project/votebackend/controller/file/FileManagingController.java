package project.votebackend.controller.file;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.votebackend.service.file.FileManagingService;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileManagingController {

    private final FileManagingService fileManagingService;

    //이미지 업로드
    @PostMapping("/image/upload")
    @Operation(summary = "이미지 업로드 API", description = "글작성시 이미지를 업로드합니다.")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileManagingService.storeFile(file);
        return ResponseEntity.ok(imageUrl);
    }

    //이미지 삭제
    @DeleteMapping("/image/delete")
    @Operation(summary = "이미지 삭제 API", description = "글작성시 이미지를 삭제합니다.")
    public ResponseEntity<?> deleteImage(@RequestBody Map<String, String> body) {
        String fileUrl = body.get("fileUrl");
        fileManagingService.deleteImage(fileUrl);
        return ResponseEntity.ok("삭제 완료");
    }

    // 영상 업로드
    @PostMapping("/video/upload")
    @Operation(summary = "영상 업로드 API", description = "글작성시 영상을 업로드합니다.")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        log.info("[VIDEO-UPLOAD] 요청 수신: originalName={}, size={}MB, contentType={}",
                file.getOriginalFilename(),
                String.format("%.2f", file.getSize() / 1024.0 / 1024.0),
                file.getContentType());

        String videoUrl = fileManagingService.storeVideo(file);

        log.info("[VIDEO-UPLOAD] 완료: url={}", videoUrl);
        return ResponseEntity.ok(videoUrl);
    }

    // 영상 삭제
    @DeleteMapping("/video/delete")
    @Operation(summary = "영상 삭제 API", description = "글작성시 영상을 삭제합니다.")
    public ResponseEntity<?> deleteVideo(@RequestBody Map<String, String> body) {
        String fileUrl = body.get("fileUrl");
        log.info("[VIDEO-DELETE] 요청 수신: fileUrl={}", fileUrl);

        fileManagingService.deleteVideo(fileUrl);

        log.info("[VIDEO-DELETE] 완료: fileUrl={}", fileUrl);
        return ResponseEntity.ok("삭제 완료");
    }
}
