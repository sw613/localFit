package com.example.LocalFit.community.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.example.LocalFit.community.dto.ResponseCommDto;
import com.example.LocalFit.community.entity.ChatParticipant;
import com.example.LocalFit.community.entity.Community;
import com.example.LocalFit.community.repository.ChatParticipantRepository;
import com.example.LocalFit.community.repository.CommRepository;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;
import com.example.LocalFit.meeting.entity.Meeting;
import com.example.LocalFit.meeting.service.MeetingService;
import com.example.LocalFit.user.Service.UserService;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.repository.UserRepository;

import jakarta.transaction.Transactional;


@Service
public class CommService {
	
	private final CommRepository commRepository;
	private final UserRepository userRepository;
	private final ChatParticipantRepository chatParticipantRepository;
	private final MeetingService meetingService;
	@Lazy
    private final UserService userService;
    private final StompService stompService;

	public CommService(CommRepository commRepository, 
					   @Lazy UserRepository userRepository,
					   ChatParticipantRepository chatParticipantRepository,
					   @Lazy MeetingService meetingService,    // MeetingService가 먼저 초기화되지 않으면 여기서 오류 발생함
					   UserService userService,
					   StompService stompService) {   
		this.commRepository = commRepository;
		this.userRepository = userRepository;
		this.chatParticipantRepository = chatParticipantRepository;
		this.meetingService = meetingService;   // 초기화안된 않은 프록시 객체가 주입
		this.userService = userService;
		this.stompService = stompService;
	}
	
	// 채팅방 생성	
	public Community createChatRoom(Meeting meeting) {
		Community newCommunity = new Community(meeting);	
		
		return commRepository.save(newCommunity);
	}	

	
	// 채팅방 목록 조회 (유저별)
	public List<ResponseCommDto> getAllChatRooms() {
		User user = userService.getCurrentUser();
		
		if(user == null) {
			throw new CustomException(CustomErrorCode.NOT_AUTHENTICATED);
		}
		
		List<Community> communities = commRepository.findAllCommunitiesByUser(user.getId());
		
		return communities.stream()
				.map(Community::toResponseCommDto)
				.collect(Collectors.toList());
	}
	
	
	// 특정 채팅방 조회 (유저별)
	public ResponseCommDto getChatRoom(Long roomId) {
		
		User user = userService.getCurrentUser();
		
		if(user == null) {
			throw new CustomException(CustomErrorCode.NOT_AUTHENTICATED);
		}
		
		Community community = commRepository.findCommunityByUser(roomId, user.getId())
				.orElseThrow(() ->new NoSuchElementException("Not found chatroom no: " + roomId + " or user id: " + user.getId()));
		
		return community.toResponseCommDto();
	}
	
	// 채팅방 참여 인원 추가
	@Transactional
	public ChatParticipant addChatParticipant(Long roomId, Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() ->new NoSuchElementException("Not found user id" + userId));
		
		Community community = commRepository.findById(roomId)
				.orElseThrow(() ->new NoSuchElementException("Not found chatroom no: " + roomId));
		
		ChatParticipant chatParticipant = ChatParticipant.builder()
												.user(user)
												.community(community)
												.build();
		
		community.getChatParticipants().add(chatParticipant);
		community.getMeeting().increaseCurrentPeople();    // 모임에도 인원 추가
		
		stompService.getUpdateCommunity(community.toResponseCommDto()); 
		stompService.joinNewUser(roomId.toString(), user);
		
		return chatParticipantRepository.save(chatParticipant);
	}
	
	// 채팅방 나가기 (모임에서도 나가기)
	@Transactional
	public void leaveChatRoom(Long roomId, boolean isHost) {
		User user = userService.getCurrentUser();
		
		ChatParticipant chatParticipant = chatParticipantRepository.findByCommunityIdAndUserId(roomId, user.getId())
				.orElseThrow(() ->new NoSuchElementException("Not found chatroom no: " + roomId + " or user id: " + user.getId()));
		
		chatParticipantRepository.delete(chatParticipant);
		
		Community community = commRepository.findById(roomId)
		        .orElseThrow(() -> new NoSuchElementException("Not found chatroom id: " + roomId));		

	    if (isHost) {
	    	stompService.deleteRedisData(roomId.toString());  // 레디스에서 데이터 삭제
	        deleteCommunity(roomId); // 방장이 나가면 채팅방 삭제
	        stompService.notifyChatroomDeleted(roomId.toString());    // 모든 유저에게 채팅방 삭제 알림 전송
	        return;
	    } else {
	        community.getMeeting().decreaseCurrentPeople();
	        meetingService.deleteSignupWhenLeaveChatRoomParticipant(roomId, user.getId());
	    }		
		
		stompService.getUpdateCommunity(community.toResponseCommDto());
		stompService.DeleteRedisActiveUser(roomId.toString(), user.getId().toString());    // 레디스에서 실시간 접속 유저 삭제
	}	
	
	// 유저 강제 퇴장 시키기
	@Transactional
	public void kickUser(Long roomId, Long userId) {
		ChatParticipant chatParticipant = chatParticipantRepository.findByCommunityIdAndUserId(roomId, userId)
				.orElseThrow(() ->new NoSuchElementException("Not found chatroom no: " + roomId + " or user id: " + userId));
		
		chatParticipantRepository.delete(chatParticipant);
		
		Community community = commRepository.findById(roomId)
		        .orElseThrow(() -> new NoSuchElementException("Not found chatroom id: " + roomId));
		
        community.getMeeting().decreaseCurrentPeople();
        meetingService.deleteSignupWhenLeaveChatRoomParticipant(roomId, userId);
		
        // 강퇴된 유저에게 웹소켓 알림
        stompService.notifyUserKicked(roomId.toString(), userId.toString());
        
		stompService.getUpdateCommunity(community.toResponseCommDto());
		stompService.resetUnreadMessage(roomId.toString(), userId.toString());    // 레디스에서 안읽은 메세지 삭제
		stompService.DeleteRedisActiveUser(roomId.toString(), userId.toString());    // 레디스에서 실시간 접속 유저 삭제
	}
	
	
	// 채팅방 삭제 (모임도 같이 삭제됨)
	@Transactional
	public void deleteCommunity(Long roomId) {
		Community community = commRepository.findById(roomId).orElseThrow(() ->new NoSuchElementException("Not found chatroom id: " + roomId));
		
		commRepository.delete(community);
		meetingService.deleteMeetingWhenHostLeavesChatRoom(roomId);   // 모임도 삭제
	}
	
	// 채팅방만 삭제
	public void deleteOnlyCommunity(Long roomId) {
		Community community = commRepository.findById(roomId).orElseThrow(() ->new NoSuchElementException("Not found chatroom id: " + roomId));
		
		commRepository.delete(community); 
	}
}
