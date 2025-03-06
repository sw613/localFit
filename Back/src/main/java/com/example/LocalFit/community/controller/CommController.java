package com.example.LocalFit.community.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.LocalFit.community.dto.RequestChatMessageDto;
import com.example.LocalFit.community.dto.ResponseChatMessageDto;
import com.example.LocalFit.community.dto.ResponseCommDto;
import com.example.LocalFit.community.service.CommService;
import com.example.LocalFit.community.service.KafkaConsumerService;
import com.example.LocalFit.community.service.KafkaProducerService;
import com.example.LocalFit.community.service.StompService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/chat")
public class CommController {

    private final KafkaProducerService kafkaProducerService;
    private final CommService commService;
    private final KafkaConsumerService kafkaConsumerService;
    private final StompService stompService;
    
    public CommController(KafkaProducerService kafkaProducerService, 
    					  CommService commService, 
    					  KafkaConsumerService kafkaConsumerService,
    					  StompService stompService) {
    	this.kafkaProducerService = kafkaProducerService;
    	this.commService = commService;
    	this.kafkaConsumerService = kafkaConsumerService;
    	this.stompService = stompService;
    }
    
    // 전체 채팅방 목록 조회 (유저별)
    @GetMapping("/list")
	public ResponseEntity<List<ResponseCommDto>> getAllChatRooms() {
    	
    	List<ResponseCommDto> responseCommDtos = commService.getAllChatRooms();
    	
    	return ResponseEntity.ok(responseCommDtos);
    }
    
    // 특정 채팅방 조회 (유저별)
    @GetMapping("/{roomId}")
    public ResponseEntity<ResponseCommDto> getChatRoom(@PathVariable Long roomId) {
    	ResponseCommDto responseCommDto = commService.getChatRoom(roomId);
    	
    	return ResponseEntity.ok(responseCommDto);
    }    
    
	// 특정 채팅방 채팅 메세지 조회
	@GetMapping("/{roomId}/messages")
	public ResponseEntity<List<ResponseChatMessageDto>> getChatRommMessage(@PathVariable Long roomId) {
		List<ResponseChatMessageDto> chatMessageDtos = kafkaConsumerService.getChatMessages(roomId);
		
		return ResponseEntity.ok(chatMessageDtos);
	}
	
//	// 특정 채팅방 채팅 메세지 조회
//    @GetMapping("/{roomId}/messages")
//    public Flux<ResponseChatMessageDto> getChatRoomMessages(@PathVariable Long roomId) {
//        return kafkaConsumerService.getChatMessages(roomId);
//    }
	
	
	// 메세지 전송
	@MessageMapping("/chatRoom/{roomId}")
	public void SendMessage(@DestinationVariable String roomId, RequestChatMessageDto chatMessageDto) {
		kafkaProducerService.send(chatMessageDto);
	}
	
	// 채팅방 나가기(모임 탈퇴)
	@DeleteMapping("/delete/{roomId}")
	public ResponseEntity<String> leaveChatRoom(@PathVariable Long roomId, @RequestParam boolean isHost) {
		commService.leaveChatRoom(roomId, isHost);
		
		return ResponseEntity.ok("leave chatroom successfully");
	}
	
	// 유저 강제 퇴장 시키기(방장만 가능)
	@PostMapping("/kickUser/{roomId}")
	public ResponseEntity<String> kickUser(@PathVariable Long roomId, @RequestParam Long userId) {
		commService.kickUser(roomId, userId);
		
		return ResponseEntity.ok("kick user successfully");
	}
	
	// 채팅방 입장(실시간 유저에 추가)
	@PostMapping("/{roomId}/enter")
	public void enterChatRoom(@PathVariable String roomId) {
		stompService.enterChatRoom(roomId);
	}
	
	// 채팅방 퇴장(실시간 유저에서 삭제)
	@PostMapping("/{roomId}/exit")
	public void exitChatRoom(@PathVariable String roomId) {
		stompService.exitChatRoom(roomId);
	}
	
    // 현재 채팅방 접속자 목록 조회
    @GetMapping("/{roomId}/activeUsers")
    public ResponseEntity<Set<String>> getActiveUsers(@PathVariable String roomId) {
        Set<String> activeUsers = stompService.getActiveUsers(roomId);
        
        return ResponseEntity.ok(activeUsers);
    }
    
    // 채팅방 안읽은 메세지 수 업데이트
    @PostMapping("/{roomId}/unread-increase")
    public void increaseUnreadMessage(@PathVariable String roomId) {
        stompService.increaseUnreadMessage(roomId);
    }
    
    // 채팅방 안읽은 메세지 수 reset
    @PostMapping("/{roomId}/unread-reset")
    public void resetUnreadMessage(@PathVariable String roomId) {
        stompService.resetUnreadMessage(roomId);
    }
    
    // 채팅방 별 안읽은 메세지 수 조회
    @GetMapping("/unread-messages")
    public ResponseEntity<Map<String, String>> getUnreadMessageCount() {
    	Map<String, String> unreadMessageCount = stompService.getUnreadMessageCounts();
    	
    	return ResponseEntity.ok(unreadMessageCount);    	
    }
    
}
