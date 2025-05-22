package project.votebackend.service.auth;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.category.Category;
import project.votebackend.domain.user.User;
import project.votebackend.domain.user.UserInterest;
import project.votebackend.dto.login.LoginRequest;
import project.votebackend.dto.login.LoginResponse;
import project.votebackend.dto.signup.UserSignupDto;
import project.votebackend.elasticSearch.UserDocument;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.CategoryException;
import project.votebackend.repository.category.CategoryRepository;
import project.votebackend.repository.user.UserInterestRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.type.ErrorCode;
import project.votebackend.util.JwtUtil;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final UserInterestRepository userInterestRepository;
    private final JwtUtil jwtUtil;
    private final ElasticsearchClient elasticsearchClient;


    // 회원가입 (아이디 및 전화번호는 중복 존재 불가)
    @Transactional
    public User registerUser(UserSignupDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new AuthException(ErrorCode.ALREADY_EXIST_NAME);
        }

        if (userRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new AuthException(ErrorCode.ALREADY_EXIST_PHONE);
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword())) // 비밀번호 암호화
                .name(dto.getName())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .birthdate(dto.getBirthdate())
                .address(dto.getAddress())
                .introduction(dto.getIntroduction())
                .profileImage(dto.getProfileImage())
                .point(0L)
                .voteScore(0L)
                .build();

        User savedUser = userRepository.save(user);

        // 관심 카테고리 저장
        if (dto.getInterestCategory() != null) {
            for (Long categoryId : dto.getInterestCategory()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
                UserInterest interest = UserInterest.builder()
                        .user(savedUser)
                        .category(category)
                        .build();
                userInterestRepository.save(interest);
            }
        }

        //Elasticsearch에 저장
        try {
            UserDocument doc = UserDocument.fromEntity(savedUser);
            elasticsearchClient.index(i -> i
                    .index("users")
                    .id(String.valueOf(doc.getId()))
                    .document(doc)
            );
        } catch (IOException e) {
            log.error("Elasticsearch 저장 실패", e);
        }

        return savedUser;
    }

    //아이디 중복 확인
    public boolean isUsernameAvailable(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new AuthException(ErrorCode.ALREADY_EXIST_NAME);
        }
        return true;
    }

    //전화번호 중복 확인
    public boolean isPhoneAvailable(String phone) {
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new AuthException(ErrorCode.ALREADY_EXIST_PHONE);
        }
        return true;
    }

    //로그인
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getUserId());
        return new LoginResponse("success", token);
    }
}
