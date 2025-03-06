package com.example.LocalFit.lounge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.LocalFit.lounge.entity.Follow;
import com.example.LocalFit.user.entity.User;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    // 팔로우 리스트
    List<Follow> findByFollower(User user);
    
    // 팔로잉리스트
    List<Follow> findByFollowing(User user);
    
    // 특정 팔로우 관계가 존재하는지 확인
    Optional<Follow> findByFollowerAndFollowing(User follower, User followee);
    
    long countByFollowing(User user);
    long countByFollower(User user);

    void deleteByFollower_Id(Long followerId);

    void deleteByFollowing_Id(Long followingId);
}
