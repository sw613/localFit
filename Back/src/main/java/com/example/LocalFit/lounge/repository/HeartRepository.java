package com.example.LocalFit.lounge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.LocalFit.lounge.entity.Heart;

@Repository
public interface HeartRepository extends JpaRepository<Heart, Long> {
	Optional<Heart> findByFeedIdAndUserId(Long feedId, Long userId);

	//탈퇴 처리용
	void deleteByUser_Id(Long userId);
}
