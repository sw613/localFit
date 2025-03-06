package com.example.LocalFit.meeting.service;

import com.example.LocalFit.connectHashtag.service.ConnectHashtagService;
import com.example.LocalFit.meeting.entity.Meeting;
import com.example.LocalFit.meeting.repository.MeetingRepository;
import com.example.LocalFit.signupMeeting.service.SignupMeetingHelper;
import com.example.LocalFit.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingHelper {

    private final MeetingRepository meetingRepository;
    private final SignupMeetingHelper signupMeetingHelper;
    private final ConnectHashtagService connectHashtagService;

    public void whenUserDelete(User currentUser) {
        // 회원이 모임장인 모임 확인
        boolean hasHostedMeetings = meetingRepository.existsMeetingsByUser_Id(currentUser.getId());

        if (hasHostedMeetings) {
            // 모임장인 모임 직접 조회
            List<Meeting> hostedMeetings = meetingRepository.findByUser_Id(currentUser.getId());

            // 모임장인 모임 삭제 - meetingService 대신 직접 구현
            for (Meeting meeting : hostedMeetings) {
                Long meetingId = meeting.getId();

                // 모임 참가 신청 정보 삭제
                signupMeetingHelper.deleteSignupMeetingByMeetingId(meetingId);

                // 모임과 연결된 해시태그 삭제
                connectHashtagService.deleteConnectHashtagByMeetingId(meetingId);

                //모임 삭제
                meetingRepository.deleteById(meetingId);
            }
        }
    }

    // 회원탈퇴시 모임 개설되어있는지 확인하는 메서드
    @Transactional(readOnly = true)
    public boolean checkHostMeeting(Long userId) {
        return meetingRepository.existsMeetingsByUser_Id(userId);
    }
}
