package com.example.LocalFit.auth.dto;

import com.example.LocalFit.user.entity.Role;
import com.example.LocalFit.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomUserDetails implements OAuth2User, UserDetails {
    private final User user;
    private Map<String, Object> attributes;

    //JWT 인증용
    public CustomUserDetails(User user) {
        this.user = user;
    }

    // OAuth2 인증용
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // OAuth2 로그인용 팩토리 메서드
    public static CustomUserDetails create(User user, Map<String, Object> attributes) {
        return new CustomUserDetails(user, attributes);
    }

    // OAuth2User 인터페이스
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    // UserDetails 인터페이스
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role userRole = user.getRole();
        if (userRole == null) {  // null 체크
            userRole = Role.USER;  // null이면 기본값 설정
        }
        return Collections.singleton(new SimpleGrantedAuthority(userRole.getValue()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
