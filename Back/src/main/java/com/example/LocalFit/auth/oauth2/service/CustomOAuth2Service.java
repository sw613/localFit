package com.example.LocalFit.auth.oauth2.service;

import com.example.LocalFit.auth.AuthProvider;
import com.example.LocalFit.auth.oauth2.info.OAuth2InfoFactory;
import com.example.LocalFit.auth.oauth2.info.OAuth2UserInfo;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;
import com.example.LocalFit.user.entity.Role;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomOAuth2Service extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2InfoFactory.getOAuth2UserInfo(
                registrationId,
                oauth2User.getAttributes()
        );

        User user = userRepository.findByEmail(userInfo.getEmail())
                .map(existingUser -> updateUser(existingUser))
                .orElseGet(() -> registerUser(userInfo, AuthProvider.valueOf(registrationId.toUpperCase())));

        return oauth2User;
    }

    //일반 회원 이메일과 같은 이메일로 로그인 방지
    private User updateUser(User existingUser) {
        if(existingUser.getProvider() == AuthProvider.LOCAL) {
            throw new CustomException(CustomErrorCode.OAUTH_USER_EXISTS);
        }
        return existingUser;
    }

    private User registerUser(OAuth2UserInfo userInfo, AuthProvider provider) {
        User user = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .provider(provider)
                .providerId(userInfo.getId())
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }
}
