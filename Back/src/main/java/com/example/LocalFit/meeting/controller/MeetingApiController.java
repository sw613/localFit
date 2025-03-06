package com.example.LocalFit.meeting.controller;

import com.example.LocalFit.meeting.entity.Meeting;
import com.example.LocalFit.meeting.entity.MeetingRequestDto;
import com.example.LocalFit.meeting.entity.MeetingResponseDto;
import com.example.LocalFit.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/meeting")
@RequiredArgsConstructor
@Slf4j
public class MeetingApiController {

    private final MeetingService meetingService;

    @GetMapping("/listAll")
    public Page<MeetingResponseDto> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {
        return meetingService.findAll(PageRequest.of(page, size));
    }

    // 시설별 모임 목록
    @GetMapping("/list")
    public List<MeetingResponseDto> findByFacilityId(@RequestParam Long facilityId) {
        return meetingService.findByFacilityId(facilityId);
    }

    @GetMapping("/list/user")
    public List<MeetingResponseDto> findByUserId() {
        return meetingService.findByUserId();
    }

    @GetMapping("/{meetingId}")
    public MeetingResponseDto findById(@PathVariable Long meetingId) {
        return meetingService.findById(meetingId);
    }

    @PostMapping("/create")
    public MeetingResponseDto createMeeting(@Validated @RequestPart MeetingRequestDto meetingRequestDto, @RequestPart MultipartFile thumbnail) {
        return meetingService.createMeeting(meetingRequestDto, thumbnail);
    }

    @PostMapping("/update/{meetingId}")
    public MeetingResponseDto updateMeeting(@PathVariable Long meetingId, @Validated @RequestPart MeetingRequestDto meetingRequestDto, @RequestPart(required = false) MultipartFile thumbnail) {
        return meetingService.updateMeeting(meetingId, meetingRequestDto, thumbnail);
    }

    @DeleteMapping("/delete/{meetingId}")
    public void deleteMeeting(@PathVariable Long meetingId) {
        meetingService.deleteMeeting(meetingId);
    }
}
