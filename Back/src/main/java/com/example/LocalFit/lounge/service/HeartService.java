package com.example.LocalFit.lounge.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.LocalFit.lounge.entity.Feed;
import com.example.LocalFit.lounge.entity.Heart;
import com.example.LocalFit.lounge.repository.FeedRepository;
import com.example.LocalFit.lounge.repository.HeartRepository;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeartService {
    private final HeartRepository heartRepository;
    private final UserRepository userRepository;
    
    private final FeedRepository feedRepository;

    public boolean toggleHeart(Long feedId, User curUser) {
        User user = curUser;
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new RuntimeException("Feed not found"));

        // 존재하면 제거, 없으면 생성
        Optional<Heart> optionalHeart = heartRepository.findByFeedIdAndUserId(feedId, user.getId());
        if (optionalHeart.isPresent()) {
            heartRepository.delete(optionalHeart.get());
            return false; // 좋아요 해제됨
        } else {
            Heart heart = Heart.builder().feed(feed).user(user).build();
            heartRepository.save(heart);
            return true; // 좋아요 등록됨
        }
    }
}
