package com.example.LocalFit.community.dto;

import java.time.LocalTime;
import java.util.List;

import com.example.LocalFit.community.entity.ChatParticipant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseCommDto {
	private Long id;
	private String meetingTitle;
	private String content;
	private Long numberPeopleMax;
	private Long numberPeopleCur;
	private LocalTime meetingTime;
	private String meetingWeek;
	private String facilityName;
	private String thumbnail;   
	
	private List<ChatParticipant> chatParticipants;
}
