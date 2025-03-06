package com.example.LocalFit.community.entity;

import com.example.LocalFit.global.BaseEntity;
import com.example.LocalFit.user.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatParticipant extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user; 
	
	@ManyToOne
	@JoinColumn(name ="room_id", nullable = false)
	@JsonBackReference
	private Community community;
	
	@JsonProperty("isHost")   // @Getter메서드에서 자동으로 getHost()로 변환되는 거 막기 위해 설정함
	private boolean isHost;
	
	public ChatParticipant(User user, Community community, boolean isHost) {
		this.user = user;
		this.community = community;
		this.isHost = isHost;
	}      
}
