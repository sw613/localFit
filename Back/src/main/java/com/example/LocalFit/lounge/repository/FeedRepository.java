package com.example.LocalFit.lounge.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.LocalFit.lounge.entity.Feed;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
	@Query("SELECT f FROM Feed f " +
	       "LEFT JOIN f.hearts h " +
	       "GROUP BY f.id " +
	       "ORDER BY COUNT(h) DESC")
	Page<Feed> findAllOrderByHeartCountDesc(Pageable pageable);
	
    @Query("SELECT f FROM Feed f WHERE f.user.id = :userId ORDER BY f.createdDate DESC")
    List<Feed> findByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId);

    void deleteByUser_Id(Long userId);

}
