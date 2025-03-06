package com.example.LocalFit.signupMeeting.controller;


import com.example.LocalFit.signupMeeting.entity.SignupMeetingRequestDto;
import com.example.LocalFit.signupMeeting.entity.SignupMeetingResponseDto;
import com.example.LocalFit.signupMeeting.service.SignupMeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/signup_meeting")
@RequiredArgsConstructor
public class SignupMeetingApiController {

    private final SignupMeetingService signupMeetingService;

    @PostMapping("/create")
    public SignupMeetingResponseDto createSignupMeeting(@RequestBody SignupMeetingRequestDto signupMeetingRequestDto) {
        return signupMeetingService.createSignupMeeting(signupMeetingRequestDto);
    }

    @GetMapping("/list")
    public List<SignupMeetingResponseDto> getList() {
        return signupMeetingService.findByUserid();
    }

    //가입승인
    @PostMapping("/agree/{singupMeetingId}")
    public SignupMeetingResponseDto agreeSignupMeeting(@PathVariable Long singupMeetingId) {
        return signupMeetingService.agreeSignupMeeting(singupMeetingId);
    }

    // 가입신청 거절시 삭제
    @DeleteMapping("/delete/{singupMeetingId}")
    public void deleteSignupMeeting(@PathVariable Long singupMeetingId) {
        signupMeetingService.deleteSignupMeeting(singupMeetingId);
    }



}
