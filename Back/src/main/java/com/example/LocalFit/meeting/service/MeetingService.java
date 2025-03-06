package com.example.LocalFit.meeting.service;

import com.example.LocalFit.community.service.CommService;
import com.example.LocalFit.connectHashtag.entity.ConnectHashtag;
import com.example.LocalFit.connectHashtag.entity.ConnectHashtagResponseDto;
import com.example.LocalFit.connectHashtag.service.ConnectHashtagService;
import com.example.LocalFit.facility.entity.Facility;
import com.example.LocalFit.facility.repository.FacilityRepository;
import com.example.LocalFit.global.S3Service;
import com.example.LocalFit.hashtag.entity.Hashtag;
import com.example.LocalFit.hashtag.service.HashtagService;
import com.example.LocalFit.meeting.entity.ApplicationMethod;
import com.example.LocalFit.meeting.entity.Meeting;
import com.example.LocalFit.meeting.entity.MeetingRequestDto;
import com.example.LocalFit.meeting.entity.MeetingResponseDto;
import com.example.LocalFit.meeting.repository.MeetingRepository;
import com.example.LocalFit.signupMeeting.service.SignupMeetingHelper;
import com.example.LocalFit.signupMeeting.service.SignupMeetingService;
import com.example.LocalFit.user.Service.UserService;
import com.example.LocalFit.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final FacilityRepository facilityRepository;
    private final UserService userService;
    private final S3Service s3Service;
    private final HashtagService hashtagService;
    private final ConnectHashtagService connectHashtagService;
    private final SignupMeetingHelper signupMeetingHelper;

    @Lazy  // 순환 참조 방지
    private final CommService commService;

    @Transactional
    public MeetingResponseDto createMeeting(MeetingRequestDto meetingRequestDto, MultipartFile thumbnail) {

        //현재 접속중인 유저 가져오기
        User user = userService.getCurrentUser();
        Facility findFacility = facilityRepository.findById(meetingRequestDto.getFacilityId()).orElseThrow(() -> new NoSuchElementException("Not Found FacilityId: " + meetingRequestDto.getFacilityId()));

        String imageUrl = null;
        if (thumbnail != null) {
            imageUrl = s3Service.upload(thumbnail);
        }

        // 모임 생성
        Meeting meeting = Meeting.builder()
                .meetingTitle(meetingRequestDto.getMeetingTitle())
                .content(meetingRequestDto.getContent())
                .numberPeopleMin(meetingRequestDto.getNumberPeopleMin())
                .numberPeopleMax(meetingRequestDto.getNumberPeopleMax())
                .numberPeopleCur(1L)
                .meetingTime(meetingRequestDto.getMeetingTime())
                .numberAgeMin(meetingRequestDto.getNumberAgeMin())
                .numberAgeMax(meetingRequestDto.getNumberAgeMax())
                .applicationMethod(ApplicationMethod.from(meetingRequestDto.getApplicationMethod()))
                .meetingWeek(meetingRequestDto.getMeetingWeek())
                .thumbnail(imageUrl)
                .user(user)
                .facility(findFacility)
                .build();

        meetingRepository.save(meeting);
        commService.createChatRoom(meeting);

        //해쉬태그 생성
        List<Hashtag> hashtagList = hashtagService.createHashtag(meetingRequestDto.getHashtags());
        List<ConnectHashtag> connectHashtagList = connectHashtagService.createConnectHashtag(meeting, hashtagList);

        meeting.updateConnectHashtag(connectHashtagList);

        return meeting.meetingToMeetingResponseDto();
    }

    @Transactional(readOnly = true)
    public Page<MeetingResponseDto> findAll(Pageable pageable) {
        return meetingRepository.findAll(pageable)
                .map(Meeting::meetingToMeetingResponseDto);
    }

    @Transactional(readOnly = true)
    public MeetingResponseDto findById(Long meetingId) {
        List<ConnectHashtagResponseDto> connectHashtagResponseDtoList = connectHashtagService.findByMeetingId(meetingId);
        String hashtag = null;
        if (!connectHashtagResponseDtoList.isEmpty()) {
            hashtag = connectHashtagResponseDtoList.get(0).getHashtag();
        }
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NoSuchElementException("Not Found meeting id : " + meetingId))
                .meetingToMeetingResponseDto();
    }

    @Transactional(readOnly = true)
    public Meeting getMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NoSuchElementException("Not Found meeting id : " + meetingId));
    }

    // 시설별 모임목록 반환
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> findByFacilityId(Long facilityId) {
        Facility findFacility = facilityRepository.findById(facilityId).orElseThrow(() -> new NoSuchElementException("Not Found facilityId: " + facilityId));

        return meetingRepository.findByFacility(findFacility).stream()
                .map(Meeting::meetingToMeetingResponseDto)
                .toList();
    }

    //내가 방장인 모임목록 반환
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> findByUserId() {
        //현재 접속중인 유저 가져오기
        User user = userService.getCurrentUser();

        return meetingRepository.findByUser_Id(user.getId()).stream()
                .map(Meeting::meetingToMeetingResponseDto)
                .toList();
    }

    // 모임 수정
    @Transactional
    public MeetingResponseDto updateMeeting(Long meetingId, MeetingRequestDto meetingRequestDto, MultipartFile thumbnail) {

        Meeting findMeeting = meetingRepository.findById(meetingId).orElseThrow(() -> new NoSuchElementException("Not Found Meeting id: " + meetingId));

        //해쉬태그 수정
        List<ConnectHashtag> connectHashtagList = null;
        List<String> hashtagStrList = meetingRequestDto.getHashtags();

        if (hashtagStrList != null) {  // 해시태그를 수정할때
            List<Hashtag> hashtagList = hashtagService.createHashtag(meetingRequestDto.getHashtags());
            connectHashtagList = connectHashtagService.updateConnectHashtag(findMeeting, hashtagList);
        } else {  // 해시태그를 모두 지울때
            connectHashtagService.deleteConnectHashtagByMeetingId(findMeeting.getId());
        }

        findMeeting.updateConnectHashtag(connectHashtagList);

        findMeeting.updateMeeting(meetingRequestDto);

        return findMeeting.meetingToMeetingResponseDto();
    }

    //모임 삭제
    @Transactional
    public void deleteMeeting(Long meetingId) {
        // 모임 찾기
        Meeting findMeeting = meetingRepository.findById(meetingId).orElseThrow(() -> new NoSuchElementException("Not Found Meeting id: " + meetingId));

        // signup(모임 참가 여부) 모두 삭제
        signupMeetingHelper.deleteSignupMeetingByMeetingId(findMeeting.getId());

        // 모임과 연결된 해시태그 삭제
        connectHashtagService.deleteConnectHashtagByMeetingId(findMeeting.getId());

        // 채팅방 퇴장 호출
        commService.deleteOnlyCommunity(meetingId);

        meetingRepository.delete(findMeeting);
    }

    // 채팅방쪽에서 모임장이 채팅 나가기 했을때
    @Transactional
    public void deleteMeetingWhenHostLeavesChatRoom(Long meetingId) {
        // 모임 찾기
        Meeting findMeeting = meetingRepository.findById(meetingId).orElseThrow(() -> new NoSuchElementException("Not Found Meeting id: " + meetingId));

        // signup(모임 참가 여부) 모두 삭제
        signupMeetingHelper.deleteSignupMeetingByMeetingId(findMeeting.getId());

        // 모임과 연결된 해시태그 삭제
        connectHashtagService.deleteConnectHashtagByMeetingId(findMeeting.getId());

        meetingRepository.delete(findMeeting);
    }

    // 일반 회원이 채팅방 나갈때
    @Transactional
    public void deleteSignupWhenLeaveChatRoomParticipant(Long meetingId, Long userId) {
        signupMeetingHelper.deleteByMeetingIdAndUserId(meetingId, userId);
    }

    // 모임장 id 리턴
    @Transactional(readOnly = true)
    public Long getMeetingUserId(Long meetingId) {
        Meeting findMeeting = meetingRepository.findById(meetingId).orElseThrow(() -> new NoSuchElementException("Not Found Meeting id: " + meetingId));
        return findMeeting.getUser().getId();
    }

    @Transactional(readOnly = true)
    public boolean checkMaxParticipantsExceeded(Long meetingid) {
        Optional<Meeting> optionalMeeting = meetingRepository.findIfAvailable(meetingid);

        if (optionalMeeting.isPresent()) {
            return true;
        } else {
            return false;
        }
    }
}