package com.example.LocalFit.admin.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.LocalFit.lounge.dto.FeedResponseDTO;
import com.example.LocalFit.lounge.entity.Feed;
import com.example.LocalFit.lounge.repository.FeedRepository;
import com.example.LocalFit.lounge.service.FeedService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/lounge")
public class AdminLoungeController {
    private final FeedService feedService;
    private final FeedRepository feedRepository;
    
    @GetMapping("/feeds")
    public ResponseEntity<List<FeedResponseDTO>> getFeedList() {
        List<Feed> feeds = feedRepository.findAll();
        List<FeedResponseDTO> feedDTOs = feeds.stream()
                                              .map(FeedResponseDTO::new)
                                              .collect(Collectors.toList());
        return ResponseEntity.ok(feedDTOs);
    }
    
    @DeleteMapping("/feeds/{feedId}")
    public void deleteFeed(@PathVariable Long feedId) {
        feedService.DeleteFeed(feedId);
    }
}

