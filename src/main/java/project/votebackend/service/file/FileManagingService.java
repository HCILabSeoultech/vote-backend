package project.votebackend.service.file;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class FileManagingService {

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain; // CloudFront 도메인

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;       // S3 버킷 이름

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;        // AWS Access Key

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;        // AWS Secret Key

    @Value("${cloud.aws.region.static}")
    private String region;           // AWS 리전 (ex: ap-northeast-2)

    private S3Client s3Client;       // S3 클라이언트 객체

    // 빈 초기화 후 S3Client 생성
    @PostConstruct
    public void init() {
        s3Client = S3Client.builder()
                .region(Region.of(region)) // 리전 설정
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey))) // 인증 정보 설정
                .build();
    }

    // 이미지 파일을 S3에 업로드하고 CloudFront URL 반환
    public String storeFile(MultipartFile file) {
        try {
            // 고유한 파일명 생성 (UUID + 원래 파일명)
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String key = "images/" + fileName; // S3 내부 경로

            // 이미지 리사이즈 및 압축 (최대 1080x1080, JPEG, 품질 50%)
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(1080, 1080)       // 비율 유지하며 리사이즈
                    .outputFormat("jpg")    // JPEG 형식으로 변환
                    .outputQuality(0.5)     // 50% 품질
                    .toOutputStream(os);

            byte[] resizedImage = os.toByteArray(); // 변환된 이미지 바이트

            // S3 업로드 요청 생성
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("image/jpeg")
                    .build();

            // S3에 이미지 업로드
            s3Client.putObject(putRequest, RequestBody.fromBytes(resizedImage));

            // CloudFront 도메인을 붙여 반환 (정적 리소스 최적화)
            return "https://" + cloudFrontDomain + "/" + key;

        } catch (IOException e) {
            // 이미지 리사이즈 또는 업로드 실패 시 예외 처리
            throw new RuntimeException("S3 이미지 업로드 실패", e);
        }
    }

    // 이미지 삭제 (프로필 변경, 게시글 수정/삭제 시 사용)
    public void deleteImage(String fileUrl) {
        try {
            // CloudFront URL에서 S3 키 추출 (ex: images/uuid_filename.jpg)
            String key = fileUrl.substring(fileUrl.indexOf("images/"));

            // 삭제 요청 생성
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // S3에서 객체 삭제
            s3Client.deleteObject(deleteRequest);

        } catch (Exception e) {
            // 삭제 실패 시 예외 처리
            throw new RuntimeException("S3 이미지 삭제 실패", e);
        }
    }

    public String storeVideo(MultipartFile file) {
        try {
            if (file.getSize() > 50 * 1024 * 1024) {
                log.warn("[VIDEO-STORE] 업로드 파일 용량 초과: {}MB",
                        String.format("%.2f", file.getSize() / 1024.0 / 1024.0));
                throw new IllegalArgumentException("50MB를 초과한 파일은 업로드할 수 없습니다.");
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String videoKey = "videos/" + fileName;

            log.info("[VIDEO-STORE] 영상 업로드 시작: key={}", videoKey);

            // S3에 영상 업로드
            PutObjectRequest videoRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(videoKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(videoRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("[VIDEO-STORE] 영상 업로드 완료: key={}", videoKey);

            String url = "https://" + cloudFrontDomain + "/" + videoKey;
            log.info("[VIDEO-STORE] 최종 URL 반환: {}", url);

            return url;

        } catch (IOException e) {
            log.error("[VIDEO-STORE] 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("영상 업로드 실패", e);
        }
    }

    public void deleteVideo(String fileUrl) {
        try {
            String key = fileUrl.substring(fileUrl.indexOf("videos/"));
            String thumbnailKey = key.replace("videos/", "thumbnails/").replaceAll("\\..+$", ".jpg");

            log.info("[VIDEO-DELETE] 영상 삭제 시작: key={}", key);
            log.info("[VIDEO-DELETE] 썸네일 삭제 시작: key={}", thumbnailKey);

            DeleteObjectRequest deleteVideo = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            DeleteObjectRequest deleteThumb = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(thumbnailKey)
                    .build();

            s3Client.deleteObject(deleteVideo);
            s3Client.deleteObject(deleteThumb);

            log.info("[VIDEO-DELETE] 삭제 완료: key={}, thumbnailKey={}", key, thumbnailKey);
        } catch (Exception e) {
            log.error("[VIDEO-DELETE] 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("영상 삭제 실패", e);
        }
    }
}

