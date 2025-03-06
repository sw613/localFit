package com.example.LocalFit.admin.service;

import com.example.LocalFit.auth.AuthProvider;
import com.example.LocalFit.auth.oauth2.service.OAuth2UserService;
import com.example.LocalFit.auth.service.RedisTokenService;
import com.example.LocalFit.community.entity.Community;
import com.example.LocalFit.community.repository.CommRepository;
import com.example.LocalFit.community.service.CommunityHelper;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;
import com.example.LocalFit.lounge.repository.FeedRepository;
import com.example.LocalFit.lounge.repository.FollowRepository;
import com.example.LocalFit.lounge.repository.HeartRepository;
import com.example.LocalFit.meeting.repository.MeetingRepository;
import com.example.LocalFit.meeting.service.MeetingHelper;
import com.example.LocalFit.user.dto.UserResDto;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.entity.UserImg;
import com.example.LocalFit.user.repository.UserImgRepository;
import com.example.LocalFit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final UserRepository userRepository;
    private final RedisTokenService redisTokenService;
    private final OAuth2UserService oauth2UserService;
    private final HeartRepository heartRepository;
    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;
    private final UserImgRepository userImgRepository;
    private final CommRepository commRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingHelper meetingHelper;
    private final CommunityHelper communityHelper;

    @Transactional(readOnly = true)
    public List<UserResDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResDto::from)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        // 사용자 통계
        dashboardData.put("totalUsers", userRepository.count());

        // 모임 통계
        dashboardData.put("totalMeetings", meetingRepository.count());

        // 피드 통계
        dashboardData.put("totalFeeds", feedRepository.count());

        // 필요한 경우 더 많은 대시보드 데이터를 추가할 수 있습니다
        return dashboardData;
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_NOT_FOUND));

        log.info("관리자에 의한 사용자 삭제 시작: {}", user.getEmail());

        try {
            // OAuth2 사용자인 경우 연동 해제 처리
            if (user.getProvider() != AuthProvider.LOCAL) {
                // 관리자 권한으로 OAuth2 연동 해제 처리
                oauth2UserService.processAdminOAuth2UserDeletion(user);
            }

            // 사용자가 모임장인지 확인
            boolean isHost = meetingHelper.checkHostMeeting(user.getId());
            // 모임장인 경우 관련 모임 처리
            if (isHost) {
                meetingHelper.whenUserDelete(user);
            }

            //회원이 참여 중인 모든 채팅방 조회
            List<Community> userCommunities = commRepository.findAllCommunitiesByUser(user.getId());
            // 참여 중인 모든 채팅방에서 나가기 처리
            for (Community community : userCommunities) {
                communityHelper.leaveChatRoom(community.getId(), isHost, user.getId());
            }

            //회원 관련 데이터 삭제
            heartRepository.deleteByUser_Id(user.getId());
            feedRepository.deleteByUser_Id(user.getId());
            followRepository.deleteByFollower_Id(user.getId());
            followRepository.deleteByFollowing_Id(user.getId());

            // 사용자 이미지 삭제
            Optional<UserImg> userImgOpt = userImgRepository.findByUserId(user.getId());
            userImgOpt.ifPresent(userImg -> userImgRepository.delete(userImg));

            // 토큰 삭제
            redisTokenService.deleteRefreshToken(user.getEmail());

            // 사용자 삭제
            userRepository.delete(user);
            userRepository.flush();
            log.info("관리자에 의한 사용자 삭제 성공: {}", user.getEmail());
        } catch (Exception e) {
            log.error("관리자에 의한 사용자 삭제 중 에러 발생: {}", user.getEmail(), e);
            throw new CustomException(CustomErrorCode.USER_DELETION_FAILED);
        }
    }
}