package com.example.LocalFit.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.LocalFit.user.entity.UserImg;

public interface UserImgRepository extends JpaRepository<UserImg, Long> {
	// 라운지 마이페이지에서 가져오는 함수입니다 
	Optional<UserImg> findByUserId(Long userId);

	void deleteByUserId(Long userId);
}
