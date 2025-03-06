package com.example.LocalFit.community.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.LocalFit.community.dto.ResponseCommDto;
import com.example.LocalFit.global.BaseEntity;
import com.example.LocalFit.meeting.entity.Meeting;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Community extends BaseEntity {
	
	@Id  
	@Column(name = "room_id") 
	private Long id;    // meeting id와 동일하게 사용할 예정
	
	@OneToOne
    @JoinColumn(name = "meeting_id", nullable = false)
	private Meeting meeting;
	
	@OneToMany(mappedBy = "community", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<ChatParticipant> chatParticipants;
	
	public Community(Meeting meeting) {
		this.id = meeting.getId();
		this.meeting = meeting;
		
		// 첫 모임 참여자 = 주최자
		this.chatParticipants = new ArrayList<>();
		chatParticipants.add(new ChatParticipant(meeting.getUser(), this, true));
	}
	
	
	public ResponseCommDto toResponseCommDto() { 
		return ResponseCommDto.builder()
				.id(id)
				.meetingTitle(meeting.getMeetingTitle())
				.content(meeting.getContent())
				.numberPeopleCur(meeting.getNumberPeopleCur())
				.numberPeopleMax(meeting.getNumberPeopleMax())
				.meetingTime(meeting.getMeetingTime())
				.meetingWeek(meeting.getMeetingWeek())
				.facilityName(meeting.getFacility().getName())
				.thumbnail(meeting.getThumbnail())
				.chatParticipants(chatParticipants) 
				.build();
	}
	
}
