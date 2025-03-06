package com.example.LocalFit.lounge.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.LocalFit.lounge.dto.FollowerResponseDTO;
import com.example.LocalFit.lounge.entity.Follow;
import com.example.LocalFit.lounge.repository.FollowRepository;
import com.example.LocalFit.lounge.service.FollowService;
import com.example.LocalFit.user.Service.UserService;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lounge")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final FollowRepository followRepository; 

    @PostMapping("/follow/{followingId}")
    public ResponseEntity<?> follow(@PathVariable Long followingId) {

        Long followerId = userService.getCurrentUser().getId(); 
        followService.followUser(followerId, followingId);
        return ResponseEntity.ok("팔로우 완료");
    }

    @PostMapping("/unfollow/{followingId}")
    public ResponseEntity<?> unfollow(@PathVariable Long followingId) {
        Long followerId = userService.getCurrentUser().getId(); 
        followService.unfollowUser(followerId, followingId);
        return ResponseEntity.ok("언팔로우 완료");
    }
    

    @GetMapping("/user/{userId}/followers")
    public ResponseEntity<List<FollowerResponseDTO>> getFollowers(@PathVariable Long userId) {
        User user = userRepository.findById(userId).get();
        
        List<Follow> followers = followRepository.findByFollowing(user);
        List<FollowerResponseDTO> dtos = followers.stream().map(follow -> {
            User followerUser = follow.getFollower();
            String profileUrl = followerUser.getUserImg() != null
                    ? followerUser.getUserImg().getPath()
                    : "https://localfitbucket.s3.ap-northeast-2.amazonaws.com/default_profile.png";
            return FollowerResponseDTO.builder()
                    .id(followerUser.getId())
                    .nickname(followerUser.getNickname())
                    .profileImageUrl(profileUrl)
                    .build();
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{userId}/followings")
    public ResponseEntity<List<FollowerResponseDTO>> getFollowings(@PathVariable Long userId) {
        User user = userRepository.findById(userId).get();
        
        List<Follow> followings = followRepository.findByFollower(user);
        List<FollowerResponseDTO> dtos = followings.stream().map(follow -> {
            User followingUser = follow.getFollowing();
            String profileUrl = followingUser.getUserImg() != null
                    ? followingUser.getUserImg().getPath()
                    : "https://localfitbucket.s3.ap-northeast-2.amazonaws.com/default_profile.png";
            return FollowerResponseDTO.builder()
                    .id(followingUser.getId())
                    .nickname(followingUser.getNickname())
                    .profileImageUrl(profileUrl)
                    .build();
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
}
