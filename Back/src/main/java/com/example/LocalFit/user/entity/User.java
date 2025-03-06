package com.example.LocalFit.user.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.LocalFit.auth.AuthProvider;
import com.example.LocalFit.global.BaseEntity;
import com.example.LocalFit.lounge.entity.Feed;

import com.example.LocalFit.user.dto.UpdateReqDto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@SuperBuilder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity  {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;
	
	private String name;

	private String nickname;

	private String birth;

	private String gender;
	
	private String email;
	
	private String password;

	@Enumerated(EnumType.STRING)
	private AuthProvider provider;

	private String providerId;
	
	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
	@JsonBackReference
	private UserImg userImg;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonBackReference
    private List<Feed> feeds = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role = Role.USER;
	
	//라운지에서 쓰는 정보입니다
	private String intro;
	
	// Role 설정을 위한 메서드 추가
	public void assignRole(Role role) {
		this.role = role;
	}

	public void update(UpdateReqDto updateReqDto) {
		this.nickname = updateReqDto.getNickname();
		this.birth = updateReqDto.getBirth();
		this.gender = updateReqDto.getGender();
	}

	public void additionalInfo(String nickname, String birth, String gender) {

		this.nickname = nickname;
		this.birth = birth;
		this.gender = gender;
	}

	public void updatePassword(String newPassword) {
		this.password = newPassword;
	}
}
