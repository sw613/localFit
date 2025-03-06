package com.example.LocalFit.community.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.example.LocalFit.community.entity.ChatParticipant;
import com.example.LocalFit.community.entity.Community;
import com.example.LocalFit.community.repository.ChatParticipantRepository;
import com.example.LocalFit.community.repository.CommRepository;

import jakarta.transaction.Transactional;


@Service
public class CommunityHelper {
	private final CommRepository commRepository;
	private final ChatParticipantRepository chatParticipantRepository;
	//private final MeetingService meetingService;
	//private final StompService stompService;

	public CommunityHelper(CommRepository commRepository,
						   ChatParticipantRepository chatParticipantRepository) {

		this.chatParticipantRepository = chatParticipantRepository;

		this.commRepository = commRepository;
	}


	// 채팅방 나가기 (회원탈퇴 했을때)
	@Transactional
	public void leaveChatRoom(Long roomId, boolean isHost, Long userId) {

		ChatParticipant chatParticipant = chatParticipantRepository.findByCommunityIdAndUserId(roomId, userId)
				.orElseThrow(() ->new NoSuchElementException("Not found chatroom no: " + roomId + " or user id: " + userId));

		chatParticipantRepository.delete(chatParticipant);

		Community community = commRepository.findById(roomId)
				.orElseThrow(() -> new NoSuchElementException("Not found chatroom id: " + roomId));

		if (isHost) {
			deleteCommunity(roomId); // 방장이 나가면 채팅방 삭제
			return;
		} else {
			community.getMeeting().decreaseCurrentPeople();
		}
	}

	// 채팅방 삭제 (모임도 같이 삭제됨)
	@Transactional
	public void deleteCommunity(Long roomId) {
		Community community = commRepository.findById(roomId).orElseThrow(() ->new NoSuchElementException("Not found chatroom id: " + roomId));

		commRepository.delete(community);
	}

}