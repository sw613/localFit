package com.example.LocalFit.community.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.LocalFit.community.entity.Community;


@Repository
public interface CommRepository extends JpaRepository<Community, Long>{
	@Query("SELECT c FROM Community c JOIN c.chatParticipants p WHERE p.user.id = :userId")
	List<Community> findAllCommunitiesByUser(@Param("userId") Long userId);
	
	@Query("SELECT c FROM Community c WHERE c.id = :id AND EXISTS (SELECT 1 FROM ChatParticipant p WHERE p.community.id = c.id AND p.user.id = :userId)")
	Optional<Community> findCommunityByUser(@Param("id") Long id, @Param("userId") Long userId);
}
