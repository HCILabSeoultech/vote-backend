package project.votebackend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import project.votebackend.domain.user.User;
import project.votebackend.exception.AuthException;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.type.ErrorCode;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username){
        // 주어진 username으로 DB에서 사용자 정보를 조회
        User user = userRepository.findByUsername(username)
                // 사용자가 존재하지 않으면 예외 발생
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        // Spring Security에서 사용할 UserDetails 구현체(CustumUserDetails)로 변환하여 반환
        return new CustumUserDetails(user);
    }
}
