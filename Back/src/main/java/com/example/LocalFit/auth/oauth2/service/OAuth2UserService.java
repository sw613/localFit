package com.example.LocalFit.auth.oauth2.service;

import com.example.LocalFit.auth.AuthProvider;
import com.example.LocalFit.auth.dto.CustomUserDetails;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;
import com.example.LocalFit.user.dto.UserResDto;
import com.example.LocalFit.user.entity.Role;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService {
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService oauth2ClientService;
    private final WebClient webClient;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Transactional
    public UserResDto processOAuthLogin(CustomUserDetails userDetails) {
        String email = userDetails.getUsername();

        // 기존 회원인지 확인
        if (userRepository.existsByEmail(email)) {
            User existingUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.USER_NOT_FOUND));

            // Role이 없다면 기본값 설정
            if (existingUser.getRole() == null) {
                existingUser.assignRole(Role.USER);
                userRepository.save(existingUser);
            }

            // 소셜 로그인 사용자가 아닌 경우 예외 처리
            if (existingUser.getProvider() == null) {
                throw new CustomException(CustomErrorCode.DUPLICATE_EMAIL);
            }

            return UserResDto.from(existingUser);
        }

        // 새 사용자 생성
        User user = User.builder()
                .name(userDetails.getUser().getName())
                .email(email)
                .provider(AuthProvider.GOOGLE)
                .providerId(userDetails.getName())
                .nickname(null)
                .birth(null)
                .gender(null)
                .role(Role.USER)
                .feeds(new ArrayList<>())
                .build();

        return UserResDto.from(userRepository.save(user));
    }

    @Transactional
    public void processOAuth2UserDeletion(User user, HttpServletRequest request, HttpServletResponse response) {
        try {
            switch (user.getProvider()){
                case GOOGLE:
                    revokeGoogleAccess(user, request, response);
                    break;

                default:
                    throw new CustomException(CustomErrorCode.INVALID_OAUTH_PROVIDER);
            }
        } catch (Exception e) {
            log.error("OAuth2 연동 해제 실패: {}", user.getEmail(), e);
            throw new CustomException(CustomErrorCode.OAUTH_REVOCATION_FAILED);
        }
    }

    private void revokeGoogleAccess(User user, HttpServletRequest request, HttpServletResponse response) {
        try {
            OAuth2AuthorizedClient authorizedClient = oauth2ClientService
                    .loadAuthorizedClient("google", user.getEmail());

            if (authorizedClient != null){
                String accessToken = authorizedClient.getAccessToken().getTokenValue();

                // formData 정의 추가
                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                formData.add("token", accessToken);

                // 구글 revoke endpoint 호출
                webClient.post()
                        .uri("https://oauth2.googleapis.com/revoke")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData(formData))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();

                oauth2ClientService.removeAuthorizedClient("google", user.getEmail());
                authorizedClientRepository.removeAuthorizedClient("google", null, request, response);
            }
        } catch (Exception e) {
            log.error("구글 연동 해제 실패: {}", user.getEmail(), e);
            throw new CustomException(CustomErrorCode.GOOGLE_REVOCATION_FAILED);
        }
    }

    private String generateUniqueNickname(String baseName) {
        String nickname = baseName;
        int suffix = 1;

        while (userRepository.existsByNickname(nickname)) {
            nickname = baseName + suffix++;
        }

        return nickname;
    }

    @Transactional
    public void processAdminOAuth2UserDeletion(User user) {
        try {
            switch (user.getProvider()) {
                case GOOGLE:
                    // 관리자는 직접 Google 계정 연동 해제 없이 사용자 정보만 삭제
                    log.info("관리자에 의한 Google 사용자 삭제: {}", user.getEmail());
                    // 필요한 경우 OAuth2 관련 데이터 정리
                    try {
                        oauth2ClientService.removeAuthorizedClient("google", user.getEmail());
                    } catch (Exception e) {
                        log.warn("OAuth2 클라이언트 제거 실패 (무시됨): {}", e.getMessage());
                    }
                    break;

                default:
                    log.warn("지원되지 않는 OAuth 제공자: {}", user.getProvider());
                    break;
            }
        } catch (Exception e) {
            log.error("관리자에 의한 OAuth2 연동 해제 실패: {}", user.getEmail(), e);
            throw new CustomException(CustomErrorCode.OAUTH_REVOCATION_FAILED);
        }
    }
}
