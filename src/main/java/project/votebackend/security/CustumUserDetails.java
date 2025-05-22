package project.votebackend.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import project.votebackend.domain.user.User;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
public class CustumUserDetails implements UserDetails {

    // 현재 인증된 사용자 정보를 담고 있는 User 엔티티
    private final User user;

    // 사용자 ID 반환 (커스텀 메서드로 주로 서비스에서 사용)
    public Long getId() {
        return user.getUserId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자 권한 반환 (현재는 권한을 사용하지 않으므로 빈 리스트 반환)
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        // 사용자 비밀번호 반환 (인증 시 사용됨)
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // 사용자 이름(또는 이메일 등) 반환 - 로그인 식별자로 사용
        return user.getUsername(); // 필요에 따라 user.getEmail() 등으로 변경 가능
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부 (true = 만료되지 않음)
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠김 여부 (true = 잠기지 않음)
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 자격 증명(비밀번호) 만료 여부 (true = 만료되지 않음)
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 계정 활성화 여부 (true = 활성 상태)
        return true;
    }

}
