package com.example.LocalFit.signupMeeting.service;

import com.example.LocalFit.signupMeeting.entity.SignupMeeting;
import com.example.LocalFit.signupMeeting.repository.SignupMeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignupMeetingHelper {

    private final SignupMeetingRepository signupMeetingRepository;

    @Transactional
    public void deleteSignupMeetingByMeetingId(Long meetingId) {
        List<SignupMeeting> findSignupMeeting = signupMeetingRepository.findByMeeting_Id(meetingId);

        signupMeetingRepository.deleteAll(findSignupMeeting);
    }

    @Transactional
    public void deleteByMeetingIdAndUserId(Long meetingId, Long userId) {
        signupMeetingRepository.deleteByMeeting_IdAndUser_Id(meetingId, userId);
    }
}
