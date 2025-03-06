package com.example.LocalFit.lounge.service;

import org.springframework.stereotype.Service;

import com.example.LocalFit.lounge.entity.Follow;
import com.example.LocalFit.lounge.repository.FollowRepository;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void followUser(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다"));
        
        if (followRepository.findByFollowerAndFollowing(follower, following).isPresent()) {
            throw new RuntimeException("이미 팔로우 중입니다.");
        }
        
        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();
        followRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다"));
        
        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new RuntimeException("팔로우 중인 사용자가 아닙니다."));
        
        followRepository.delete(follow);
    }
}
