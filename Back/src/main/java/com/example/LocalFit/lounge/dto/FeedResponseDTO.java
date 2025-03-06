package com.example.LocalFit.lounge.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.LocalFit.lounge.entity.Feed;
import com.example.LocalFit.lounge.entity.FeedHashTag;
import com.example.LocalFit.lounge.entity.FeedImg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponseDTO {
    private Long id;
    private String description;
    private int view;
    private String thumbnail;
    private List<String> hashtags;
    private Long userId;
    private String userNickname;
    private String userProfileImg;
    private List<String> images;
    private List<CommentResponseDTO> comments;
    private boolean loggedIn;
    private LocalDateTime createdDate;

    private int heartCount;
    private boolean liked;
    private boolean editable;
    
    public FeedResponseDTO(Feed feed) {
        this(feed, null, null);
    }
    
    public FeedResponseDTO(Feed feed, Long currentUserId, String ProfilUrl) {
        this.id = feed.getId();
        this.description = feed.getDescription();
        this.view = feed.getView();
        this.createdDate = feed.getCreatedDate();
        
        this.userProfileImg = ProfilUrl;

        // 피드 이미지 중 첫 번째를 썸네일
        if (feed.getFeedImgs() != null && !feed.getFeedImgs().isEmpty()) {
            this.thumbnail = feed.getFeedImgs().get(0).getImage_url();
        }

        // 좋아요 개수
        this.heartCount = feed.getHearts().size();

        // 현재 사용자 ID로 하트 여부 판별
        if (currentUserId != null) {
            this.liked = feed.getHearts().stream()
                .anyMatch(h -> h.getUser().getId().equals(currentUserId));
        } else {
            // 로그인 안 된 상태라면 false
            this.liked = false;
        }
        
        // 이미지 목록
        if (feed.getFeedImgs() != null) {
            this.images = feed.getFeedImgs().stream()
                .map(FeedImg::getImage_url)
                .collect(Collectors.toList());
        } else {
            this.images = new ArrayList<>();
        }

        // 작성자 닉네임, id
        if (feed.getUser() != null) {
            this.userNickname = feed.getUser().getNickname();
            this.userId = feed.getUser().getId();
        }

        // 해시태그 목록
        if (feed.getFeedHashtags() != null && !feed.getFeedHashtags().isEmpty()) {
            this.hashtags = feed.getFeedHashtags().stream()
                    .map(FeedHashTag::getHashtag)
                    .collect(Collectors.toList());
        } else {
            this.hashtags = new ArrayList<>();
        }

        // 댓글 목록
        if (feed.getComments() != null) {
            this.comments = feed.getComments().stream()
            	.filter(comment -> comment.getParent() == null)
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());
        } else {
            this.comments = new ArrayList<>();
        }
        
        this.loggedIn = currentUserId != null;
    }
}
