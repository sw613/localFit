package com.example.LocalFit.community.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.LocalFit.community.entity.ChatParticipant;


@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
	@Query("SELECT cp FROM ChatParticipant cp JOIN cp.community c JOIN cp.user u WHERE c.id = :roomId AND u.id = :userId") 
	Optional<ChatParticipant> findByCommunityIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
