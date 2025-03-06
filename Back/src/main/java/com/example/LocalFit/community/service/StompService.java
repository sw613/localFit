package com.example.LocalFit.community.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.common.TemplateAwareExpressionParser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.LocalFit.community.dto.ResponseCommDto;
import com.example.LocalFit.community.repository.CommRepository;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;
import com.example.LocalFit.user.Service.UserService;
import com.example.LocalFit.user.dto.UserResDto;
import com.example.LocalFit.user.entity.User;


@Service
public class StompService {
	private final SimpMessagingTemplate template;
	
	private final RedisTemplate<String, Object> redisTemplate;
	private final UserService userService;
	private final CommRepository commRepository;
	
	private static final String CHAT_ROOM_ACTIVE_USER = "chatRoom:%s:activeUser";
	private static final String UNREAD_MESSAGE = "chatRoom:%s:User:%s:unread";
	private static final String CHAT_ROOM_MESAAGES = "chatRoom:%s:messages";
	
	
	public StompService(SimpMessagingTemplate template, 
						RedisTemplate<String, Object> redisTemplate,
						UserService userService,
						CommRepository commRepository) {
			this.template = template;
			this.redisTemplate = redisTemplate;
			this.userService = userService;
			this.commRepository = commRepository;
	}
	
	
	// 채팅방 접속 시 실시간 접속 유저에 추가
	public void enterChatRoom(String roomId) {
		validateRoomId(roomId);
		
		String key = String.format(CHAT_ROOM_ACTIVE_USER, roomId);
		
		User user = userService.getCurrentUser();
		
		if(user == null) {
			throw new CustomException(CustomErrorCode.NOT_AUTHENTICATED);
		}
		
		redisTemplate.opsForSet().add(key, user.getId().toString());
		//redisTemplate.expire(key, Duration.ofHours(12));  // 12시간 유지
		
		template.convertAndSend("/sub/" + roomId.toString() + "/activeUsers", getActiveUsers(roomId.toString())); // 현재 activeUsers 목록을 클라이언트로 전송 (kafka 사용할 필요 없음)
	}
	
	
	// 채팅방 나가면 실시간 접속 유저에서 삭제
	public void exitChatRoom(String roomId) {
		validateRoomId(roomId);
		
		String key = String.format(CHAT_ROOM_ACTIVE_USER, roomId);
		
		User user = userService.getCurrentUser();
		
		if(user == null) {
			throw new CustomException(CustomErrorCode.NOT_AUTHENTICATED);
		}
		
		redisTemplate.opsForSet().remove(key, user.getId().toString());
		
		template.convertAndSend("/sub/" + roomId.toString() + "/activeUsers", getActiveUsers(roomId.toString()));   // 현재 activeUsers 목록을 클라이언트로 전송 (kafka 사용할 필요 없음)
	}
	
	// 채팅방 탈퇴 시 실시간 접속 유저 redis에서 삭제
	public void DeleteRedisActiveUser(String roomId, String userId) {
		validateRoomId(roomId);
		validateUserId(userId);
		
		String key = String.format(CHAT_ROOM_ACTIVE_USER, roomId);
		
		redisTemplate.opsForSet().remove(key, userId);
		
		template.convertAndSend("/sub/" + roomId.toString() + "/activeUsers", getActiveUsers(roomId.toString()));   // 현재 activeUsers 목록을 클라이언트로 전송 (kafka 사용할 필요 없음)
	}
	
	// 채팅방 실시간 접속자 목록 조회
	public Set<String> getActiveUsers(String roomId) {
		validateRoomId(roomId);
		
		String key = String.format(CHAT_ROOM_ACTIVE_USER, roomId);
		Set<Object> members = redisTemplate.opsForSet().members(key);
		
		return members.stream()
				.map(Object::toString)
				.collect(Collectors.toSet());
	}
	
	// 채팅방 유저 목록 실시간 업데이트
	public void getUpdateCommunity(ResponseCommDto responseCommDto) {
	    Map<String, Object> updateUsers = new HashMap<>();
	    updateUsers.put("updateUsers",responseCommDto);
		
		template.convertAndSend("/sub/" + responseCommDto.getId().toString() + "/updateChatUsers", updateUsers);
	}
	
	// 채팅방에 새로운 유저가 입장 했을 때
	public void joinNewUser(String roomId, User user) {
		validateRoomId(roomId);
		
	    Map<String, String> newUserData = new HashMap<>();
	    newUserData.put("nickname", user.getNickname());
	    
	    template.convertAndSend("/sub/" + roomId + "/newUser", newUserData);
	}
	
	
	// 채팅방 안읽은 메세지 수 업데이트 (Redis에 저장)
	public void increaseUnreadMessage(String roomId) {
		validateRoomId(roomId);
		
		// 현재 채팅방에 접속한 유저 id 가져오기
		Set<String> userIds = getActiveUsers(roomId);
		Set<String> allUsers = commRepository.findById(Long.parseLong(roomId))
				.map(comm -> comm.getChatParticipants().stream()
						.map(p-> p.getUser().getId().toString())
						.collect(Collectors.toSet()))
				.orElse(Collections.emptySet());
		
		// 현재 접속하지 않은 유저의 unreadMessage count++하기		
		for(String userId : allUsers) {
			if(!userIds.contains(userId)) {
				String key = String.format(UNREAD_MESSAGE, roomId, userId);
	            Long newCount = redisTemplate.opsForValue().increment(key); // value++ or key 없으면 1로 초기화

	            // 변경된 특정 채팅방의 메시지 카운트만 전송
	            Map<String, String> updateData = new HashMap<>();
	            updateData.put(roomId, newCount.toString());
	            template.convertAndSend("/sub/" + userId + "/updateUnread", updateData);	
			}
		}
	}
	
	
	// 채팅방 안읽은 메세지 수 reset (Redis에 저장)
	public void resetUnreadMessage(String roomId) {
		validateRoomId(roomId);
		
		User user = userService.getCurrentUser();
		
		if(user == null) {
			throw new CustomException(CustomErrorCode.NOT_AUTHENTICATED);
		}
		
		String key = String.format(UNREAD_MESSAGE, roomId, user.getId().toString());		
		redisTemplate.delete(key);		
		
	    // 변경된 특정 채팅방의 메시지 카운트만 전송 (초기화된 값은 0)
	    Map<String, String> updateData = new HashMap<>();
	    updateData.put(roomId, "0");
	    template.convertAndSend("/sub/" + user.getId().toString() + "/updateUnread", updateData);
	}
	
	// 강퇴당한 유저 채팅방 안읽은 메세지 수 reset (Redis에서 삭제)
	public void resetUnreadMessage(String roomId, String userId) {
		validateRoomId(roomId);
		validateUserId(userId);
		
		String key = String.format(UNREAD_MESSAGE, roomId, userId);		
		redisTemplate.delete(key);		
		
	    // 변경된 특정 채팅방의 메시지 카운트만 전송 (초기화된 값은 0)
	    Map<String, String> updateData = new HashMap<>();
	    updateData.put(roomId, "0");
	}	
	
	
	// 채팅별 안읽은 메세지 수 조회
	public Map<String, String> getUnreadMessageCounts() {
		User user = userService.getCurrentUser();
		
		if(user == null) {
			throw new CustomException(CustomErrorCode.NOT_AUTHENTICATED);
		}
		
		// 로그인 한 유저가 속한 채팅방 id 가져오기
		List<String> roomIds = commRepository.findAllCommunitiesByUser(user.getId()).stream()
				.map(comm -> comm.getId().toString())
				.collect(Collectors.toList());
		
		Map<String, String> unreadMessageCount = new HashMap<>();
		
		for(String roomId : roomIds) {
			String key = String.format(UNREAD_MESSAGE, roomId, user.getId().toString());	
			Object count = redisTemplate.opsForValue().get(key);
			
			if(count != null) {
				unreadMessageCount.put(roomId, count.toString());	
			}	
		}		
		
		return unreadMessageCount;
	}
	
	
	// 채팅방 삭제 시 레디스 데이터 삭제
	public void deleteRedisData(String roomId) {
		validateRoomId(roomId);

        // 메세지 삭제
		String messagesKey = String.format(CHAT_ROOM_MESAAGES, roomId);
		redisTemplate.delete(messagesKey);
		
        // 안읽은 메세지 수 삭제
		Set<String> allUsers = commRepository.findById(Long.parseLong(roomId))
				.map(comm -> comm.getChatParticipants().stream()
						.map(p-> p.getUser().getId().toString())
						.collect(Collectors.toSet()))
				.orElse(Collections.emptySet());
			
		for(String userId : allUsers) {
			String unreadMessagekey = String.format(UNREAD_MESSAGE, roomId, userId);
	        
			redisTemplate.delete(unreadMessagekey);
		}
		
        // 현재 접속 유저 삭제
		String activeUsersKey = String.format(CHAT_ROOM_ACTIVE_USER, roomId);
		redisTemplate.delete(activeUsersKey);
	}
	
	// 강퇴된 유저에게 웹소켓 알림
	public void notifyUserKicked(String roomId, String userId) {
		validateUserId(userId);
		validateRoomId(roomId);
		
	    Map<String, Object> message = new HashMap<>();
	    message.put("userId", userId);
	    message.put("message", "채팅방에서 강제 퇴장되었습니다.");
		
	    template.convertAndSend("/sub/chatRoom/" + roomId + "/kicked", message);
	}
	
	// 채팅방이 삭제되었을때 모든 유저에게 웹소켓 알림
	public void notifyChatroomDeleted(String roomId) {
		validateRoomId(roomId);
		
	    template.convertAndSend("/sub/chatRoom/" + roomId + "/deleted", "채팅방이 삭제되었습니다.");
	}


	
	
	// roomdId null 체크
	public void validateRoomId(String roomId) {
		if(roomId == null || roomId.isEmpty()) {
			throw new IllegalArgumentException("Invalid roomId");
		}
	}
	
	// userId null 체크
	public void validateUserId(String userId) {
		if(userId == null || userId.isEmpty()) {
			throw new IllegalArgumentException("Invalid userId");
		}	
	}
}
