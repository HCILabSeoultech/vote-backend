package project.votebackend.service;

import jakarta.annotation.PostConstruct;
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
import java.io.IOException;
import java.util.UUID;

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
    public void deleteFile(String fileUrl) {
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
}

