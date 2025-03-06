package com.example.LocalFit.user.Service;

import com.example.LocalFit.auth.AuthProvider;
import com.example.LocalFit.auth.dto.CustomUserDetails;
import com.example.LocalFit.auth.jwt.TokenBlacklistService;
import com.example.LocalFit.auth.oauth2.service.OAuth2UserService;
import com.example.LocalFit.auth.service.RedisTokenService;
import com.example.LocalFit.community.entity.Community;
import com.example.LocalFit.community.repository.CommRepository;
import com.example.LocalFit.community.service.CommunityHelper;
import com.example.LocalFit.global.CookieUtil;
import com.example.LocalFit.lounge.repository.FeedRepository;
import com.example.LocalFit.lounge.repository.FollowRepository;
import com.example.LocalFit.lounge.repository.HeartRepository;
import com.example.LocalFit.meeting.service.MeetingHelper;
import com.example.LocalFit.user.dto.*;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;
import com.example.LocalFit.user.entity.Role;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.entity.UserImg;
import com.example.LocalFit.user.repository.UserImgRepository;
import com.example.LocalFit.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenService redisTokenService;
    private final OAuth2UserService oauth2UserService;
    private final CookieUtil cookieUtil;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserImgRepository userImgRepository;
    private final MeetingHelper meetingHelper;
    private final CommRepository commRepository;
    private final HeartRepository heartRepository;
    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;
    private final CommunityHelper communityHelper;

    @Transactional
    public UserResDto join(JoinReqDto joinReqDto) {

        if (userRepository.existsByEmail(joinReqDto.getEmail())) {
            throw new CustomException(CustomErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(joinReqDto.getNickname())) {
            throw new CustomException(CustomErrorCode.DUPLICATE_NICKNAME);
        }

        User user = User.builder()
                .name(joinReqDto.getName())
                .nickname(joinReqDto.getNickname())
                .birth(joinReqDto.getBirth())
                .gender(joinReqDto.getGender())
                .email(joinReqDto.getEmail())
                .password(passwordEncoder.encode(joinReqDto.getPassword()))
                .provider(AuthProvider.LOCAL)
                .feeds(new ArrayList<>())
                .role(Role.USER)
                .build();
        
        UserImg userImg = UserImg.builder()
        		.user(user)
        		.path("https://localfitbucket.s3.ap-northeast-2.amazonaws.com/default_profile.png")
        		.build();
        userImgRepository.save(userImg);
        
        return UserResDto.from(userRepository.save(user));
    }

    @Transactional
    public UserResDto additionalInfo(AdditionalInfoReqDto reqDto) {
        User currentUser = getCurrentUser();

        if (userRepository.existsByNickname(reqDto.getNickname())) {
            throw new CustomException(CustomErrorCode.DUPLICATE_NICKNAME);
        }

        currentUser.additionalInfo(
                reqDto.getNickname(),
                reqDto.getBirth(),
                reqDto.getGender()
        );
        User savedUser = userRepository.save(currentUser);  // 명시적 저장

        return UserResDto.from(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_NOT_FOUND));
        return UserResDto.from(user);
    }

    @Transactional(readOnly = true)
    public User getRawUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public UserResDto updateUser(Long id, UpdateReqDto updateReqDto) {
        User currentUser = getCurrentUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_NOT_FOUND));

        if (!user.getId().equals(currentUser.getId())) {
            throw new CustomException(CustomErrorCode.NOT_AUTHORIZED);
        }

        if (userRepository.existsByNicknameAndIdNot(updateReqDto.getNickname(), id)) {
            throw new CustomException(CustomErrorCode.DUPLICATE_NICKNAME);
        }

        user.update(updateReqDto);
        return UserResDto.from(user);
    }

    @Transactional
    public void updatePassword(Long id, UpdatePasswordReqDto request) {
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(id)) {
            throw new CustomException(CustomErrorCode.NOT_AUTHORIZED);
        }

        // OAuth2 사용자의 비밀번호 변경 시도 차단
        if (currentUser.getProvider() != AuthProvider.LOCAL) {
            throw new CustomException(CustomErrorCode.OAUTH2_PASSWORD_CHANGE_NOT_ALLOWED);
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new CustomException(CustomErrorCode.INVALID_PASSWORD);
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        currentUser.updatePassword(encodedNewPassword);

        // 현재 사용자의 accessToken과 refreshToken을 가져옴
        String accessToken = cookieUtil.getCookie(this.request, "accessToken")
                .map(Cookie::getValue)
                .orElse(null);

        String refreshToken = cookieUtil.getCookie(this.request, "refreshToken")
                .map(Cookie::getValue)
                .orElse(null);

        try {
            // accessToken이 있으면 블랙리스트에 추가
            if (accessToken != null) {
                tokenBlacklistService.addTokenToBlacklist(accessToken);
            }

            // refreshToken이 있으면 블랙리스트에 추가하고 Redis에서 삭제
            if (refreshToken != null) {
                tokenBlacklistService.addTokenToBlacklist(refreshToken);
                redisTokenService.deleteRefreshToken(currentUser.getEmail());
            }

            // 클라이언트 쿠키 삭제
            cookieUtil.deleteCookie(response, "accessToken");
            cookieUtil.deleteCookie(response, "refreshToken");

            log.info("비밀번호 변경 후 토큰 무효화 완료: {}", currentUser.getEmail());
        } catch (Exception e) {
            log.warn("비밀번호 변경 후 토큰 무효화 실패: {}", e.getMessage());
        }

        // 명시적 저장
        userRepository.save(currentUser);
    }

    @Transactional
    public void deleteUser(Long id, DeleteUserReqDto deleteUserReqDto) {
        User currentUser = getCurrentUser();
        log.info("탈퇴 시작: {}", currentUser.getEmail());

        if (!currentUser.getId().equals(id)) {
            throw new CustomException(CustomErrorCode.NOT_AUTHORIZED);
        }

        try {
            // 인증 처리
            if (currentUser.getProvider() == AuthProvider.LOCAL) {
                validateLocalUserDeletion(currentUser, deleteUserReqDto);
            } else {
                try {
                    oauth2UserService.processOAuth2UserDeletion(currentUser, request, response);
                } catch (Exception e) {
                    log.warn("OAuth2 연동 해제 실패, 계속 진행: {}", e.getMessage());
                }
            }
            // 사용자가 모임장인지 확인
            boolean isHost = meetingHelper.checkHostMeeting(currentUser.getId());

            //회원이 참여 중인 모든 채팅방 조회
            List<Community> userCommunities = commRepository.findAllCommunitiesByUser(currentUser.getId());
            // 참여 중인 모든 채팅방에서 나가기 처리
            for (Community community : userCommunities) {
                communityHelper.leaveChatRoom(community.getId(), isHost , currentUser.getId());
            }

            // 모임장인 경우 관련 모임 처리
            if (isHost) {
                meetingHelper.whenUserDelete(currentUser);
            }

            //회원 관련 데이터 삭제
            heartRepository.deleteByUser_Id(currentUser.getId());
            //commentRepository.deleteByUser_Id(currentUser.getId());
            feedRepository.deleteByUser_Id(currentUser.getId());
            followRepository.deleteByFollower_Id(currentUser.getId());
            followRepository.deleteByFollowing_Id(currentUser.getId());

            // 사용자 이미지 삭제
            Optional<UserImg> userImgOpt = userImgRepository.findByUserId(currentUser.getId());
            userImgOpt.ifPresent(userImg -> userImgRepository.delete(userImg));

            //토큰 정리
            redisTokenService.deleteRefreshToken(currentUser.getEmail());
            cookieUtil.deleteCookie(response, "accessToken");
            cookieUtil.deleteCookie(response, "refreshToken");
            SecurityContextHolder.clearContext();

            userRepository.delete(currentUser);
            userRepository.flush();
            log.info("탈퇴 성공: {}", currentUser.getEmail());
        } catch (Exception e) {
            log.error("탈퇴 중 에러 발생: {}", currentUser.getEmail(), e);
            throw new CustomException(CustomErrorCode.USER_DELETION_FAILED);
        }
    }

    private void validateLocalUserDeletion(User user, DeleteUserReqDto deleteUserReqDto) {
        if (deleteUserReqDto == null || deleteUserReqDto.getPassword() == null) {
            throw new CustomException(CustomErrorCode.PASSWORD_REQUIRED);
        }
        if (!passwordEncoder.matches(deleteUserReqDto.getPassword(), user.getPassword())) {
            throw new CustomException(CustomErrorCode.INVALID_PASSWORD);
        }
    }

    public UserResDto getMyPage() {
        User currentUser = getCurrentUser();
        return UserResDto.from(currentUser);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new CustomException(CustomErrorCode.NOT_AUTHENTICATED);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    public UserResDto getCurrentUserDto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new CustomException(CustomErrorCode.NOT_AUTHENTICATED);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();
        return UserResDto.from(user);

    }

    @Transactional(readOnly = true)
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
