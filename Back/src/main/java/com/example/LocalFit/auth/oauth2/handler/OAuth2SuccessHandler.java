package com.example.LocalFit.auth.oauth2.handler;

import com.example.LocalFit.auth.AuthProvider;
import com.example.LocalFit.auth.dto.CustomUserDetails;
import com.example.LocalFit.auth.oauth2.service.OAuth2AuthService;
import com.example.LocalFit.auth.oauth2.service.OAuth2UserService;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;
import com.example.LocalFit.user.dto.UserResDto;
import com.example.LocalFit.user.entity.Role;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.entity.UserImg;
import com.example.LocalFit.user.repository.UserImgRepository;
import com.example.LocalFit.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2AuthService oauth2AuthService;
    private final UserRepository userRepository;
    private final UserImgRepository userImgRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = getEmailFromOAuth2User(oauth2User);

        // 사용자가 존재하는지 확인
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // 기존 사용자
            user = userOptional.get();
        } else {
            // 새 사용자 - OAuth2User 정보로 기본 사용자 생성
            String name = getNameFromOAuth2User(oauth2User);

            user = User.builder()
                    .name(name)
                    .email(email)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(oauth2User.getName())
                    .role(Role.USER)
                    .feeds(new ArrayList<>())
                    .build();

            user = userRepository.save(user);
            // 구글 프로필 이미지 URL 가져오기
            String profileImageUrl = getProfileImageFromOAuth2User(oauth2User);

            // 프로필 이미지 저장
            UserImg userImg = UserImg.builder()
                    .user(user)
                    .path(profileImageUrl)
                    .build();
            userImgRepository.save(userImg);
        }

        // OAuth2User 속성을 사용하여 CustomUserDetails 생성
        CustomUserDetails userDetails = new CustomUserDetails(user, oauth2User.getAttributes());

        UserResDto userResDto = oAuth2UserService.processOAuthLogin(userDetails);
        User updatedUser = userRepository.findById(userResDto.getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_NOT_FOUND));

        oauth2AuthService.processOAuthSuccess(userDetails, updatedUser, response);
    }

    // 프로필 이미지 URL 가져오기 메서드 추가
    private String getProfileImageFromOAuth2User(OAuth2User oauth2User) {
        // 구글 OAuth2 사용자인 경우
        if (oauth2User instanceof DefaultOidcUser) {
            Map<String, Object> attributes = oauth2User.getAttributes();
            if (attributes.containsKey("picture")) {
                return (String) attributes.get("picture");
            }
        }

        // 프로필 이미지가 없거나 추출할 수 없는 경우 기본 이미지 사용
        return "https://localfitbucket.s3.ap-northeast-2.amazonaws.com/default_profile.png";
    }

    private String getEmailFromOAuth2User(OAuth2User oauth2User) {
        // 다양한 OAuth2 제공자를 처리할 수 있도록 이메일 추출 로직 구현
        if (oauth2User instanceof DefaultOidcUser) {
            return ((DefaultOidcUser) oauth2User).getEmail();
        }

        // 일반 OAuth2User에서 이메일 추출
        Map<String, Object> attributes = oauth2User.getAttributes();
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        }

        throw new CustomException(CustomErrorCode.EMAIL_NOT_FOUND_IN_OAUTH);
    }

    private String getNameFromOAuth2User(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        if (attributes.containsKey("name")) {
            return (String) attributes.get("name");
        }

        // 이름이 없으면 이메일의 @ 앞부분을 사용
        String email = getEmailFromOAuth2User(oauth2User);
        return email.split("@")[0];
    }
}
