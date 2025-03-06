package com.example.LocalFit.user.entity;

import com.example.LocalFit.global.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

// 빌드 오류가 나서 밑에 세개 추가함
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class UserImg extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_img_id")
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	private String path;
}
