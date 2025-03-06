package com.example.LocalFit.signupMeeting.service;

import com.example.LocalFit.community.service.CommService;
import com.example.LocalFit.meeting.entity.ApplicationMethod;
import com.example.LocalFit.meeting.entity.Meeting;
import com.example.LocalFit.meeting.repository.MeetingRepository;
import com.example.LocalFit.meeting.service.MeetingService;
import com.example.LocalFit.signupMeeting.entity.SignupMeeting;
import com.example.LocalFit.signupMeeting.entity.SignupMeetingRequestDto;
import com.example.LocalFit.signupMeeting.entity.SignupMeetingResponseDto;
import com.example.LocalFit.signupMeeting.repository.SignupMeetingRepository;
import com.example.LocalFit.user.Service.UserService;
import com.example.LocalFit.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SignupMeetingService {

    private final SignupMeetingRepository signupMeetingRepository;
    private final MeetingRepository meetingRepository;
    private final UserService userService;
    private final CommService commService;
    private final MeetingService meetingService;

    @Transactional
    public SignupMeetingResponseDto createSignupMeeting(SignupMeetingRequestDto signupMeetingRequestDto) {

        //현재 접속중인 유저 가져오기
        User user = userService.getCurrentUser();

        // 신청하려는 모임의 방장 userId 가져오기
        Long meetingUserId = meetingService.getMeetingUserId(signupMeetingRequestDto.getMeetingId());

        // 동일 회원이 동일 모임에 중복으로 신청한다면 못하게 막기 && 모임 방장이 신청한다면 막기
        if (checkUserExist(user.getId(), signupMeetingRequestDto.getMeetingId()) || user.getId().equals(meetingUserId)) {
            throw new NoSuchElementException("Already User signup Exist");
        }

        // 모임 가져오기
        Meeting findMeeting = meetingRepository.findById(signupMeetingRequestDto.getMeetingId()).orElseThrow(() -> new NoSuchElementException("Meeting Not Found"));

        // 가입규칙이 선착순일경우 승인이 이루어진것으로 함
        boolean isAgree = findMeeting.getApplicationMethod() == ApplicationMethod.FIRSTCOME;

        // 모임가입신청 생성
        SignupMeeting signupMeeting = SignupMeeting.builder()
                .user(user)
                .meeting(findMeeting)
                .greeting(signupMeetingRequestDto.getGreeting())
                .isAgree(isAgree)
                .build();

        signupMeetingRepository.save(signupMeeting);

        // 선착순일시 바로 모임 가입
        if (isAgree) agreeSignupMeeting(signupMeeting.getId());

        return signupMeeting.signupMeetingToSignupMeetingResponseDto();
    }

    @Transactional(readOnly = true)
    public boolean checkUserExist(Long userId, Long meetingId) {
        return signupMeetingRepository.existsByUser_idAndMeeting_id(userId, meetingId); // 존재하면 true 없다면 false
    }


    // userId가 방장인 모든 모임(Meeting)의 수락안된(isAgree = false) 가입신청(SignupMeeting) 가져오기
    @Transactional(readOnly = true)
    public List<SignupMeetingResponseDto> findByUserid() {
        //현재 접속중인 유저 가져오기
        User user = userService.getCurrentUser();

        return signupMeetingRepository.findMeetingListByUserId(user.getId()).stream()
                .map(SignupMeeting::signupMeetingToSignupMeetingResponseDto)
                .toList();
    }

    // 신청을 수락하였을때
    @Transactional
    public SignupMeetingResponseDto agreeSignupMeeting(Long singupMeetingId) {

        SignupMeeting findSignupMeeting = signupMeetingRepository.findById(singupMeetingId).orElseThrow(() -> new NoSuchElementException("SignupMeeting Not Found"));

        // 모임에 참여할 자리가 있는지 체크
        if (!meetingService.checkMaxParticipantsExceeded(findSignupMeeting.getMeeting().getId())) {
            throw new IllegalStateException("모임 정원이 초과되었습니다.");
        }

        // 수락
        findSignupMeeting.agreeSignupMeeting();

        // 채팅방 참여 (meetingId == roomId)
        commService.addChatParticipant(findSignupMeeting.getMeeting().getId(), findSignupMeeting.getUser().getId());

        // 저장
        signupMeetingRepository.save(findSignupMeeting);

        return findSignupMeeting.signupMeetingToSignupMeetingResponseDto();
    }

    // 신청을 거절하였을때 (삭제)
    @Transactional
    public void deleteSignupMeeting(Long singupMeetingId) {
        SignupMeeting findSignupMeeting = signupMeetingRepository.findById(singupMeetingId).orElseThrow(() -> new NoSuchElementException("SignupMeeting Not Found"));

        //삭제
        signupMeetingRepository.delete(findSignupMeeting);
    }

}
