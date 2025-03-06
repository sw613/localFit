package com.example.LocalFit.lounge.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.LocalFit.global.S3Service;
import com.example.LocalFit.global.exception.CustomException;
import com.example.LocalFit.lounge.FeedSortType;
import com.example.LocalFit.lounge.dto.CommentRequestDTO;
import com.example.LocalFit.lounge.dto.CommentResponseDTO;
import com.example.LocalFit.lounge.dto.FeedRequestDTO;
import com.example.LocalFit.lounge.dto.FeedResponseDTO;
import com.example.LocalFit.lounge.dto.HeartRequestDTO;
import com.example.LocalFit.lounge.dto.LoungeMyPageResponseDTO;
import com.example.LocalFit.lounge.entity.Comment;
import com.example.LocalFit.lounge.entity.Feed;
import com.example.LocalFit.lounge.entity.FeedHashTag;
import com.example.LocalFit.lounge.entity.FeedImg;
import com.example.LocalFit.lounge.repository.FeedRepository;
import com.example.LocalFit.lounge.repository.FollowRepository;
import com.example.LocalFit.lounge.service.CommentService;
import com.example.LocalFit.lounge.service.FeedService;
import com.example.LocalFit.lounge.service.HeartService;
import com.example.LocalFit.user.Service.UserService;
import com.example.LocalFit.user.dto.UserResDto;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.entity.UserImg;
import com.example.LocalFit.user.repository.UserImgRepository;
import com.example.LocalFit.user.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/lounge")
public class FeedAPIController {
    private final FeedService feedService;
    private final CommentService commentService;
    private final S3Service s3Service;
    private final HeartService heartService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final UserImgRepository userImgRepository;
    private final FollowRepository followRepository;

    @PostMapping(value = "/create-feed", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FeedResponseDTO> CreateFeed(
            @RequestPart("feedData") FeedRequestDTO feedDTO,
            @RequestPart("images") List<MultipartFile> imageFiles
    ) {
        User currentUser = userService.getCurrentUser();

        Feed createdFeed = Feed.builder()
                .description(feedDTO.getDescription())
                .view(0)
                .user(currentUser) 
                .build();

        // 이미지 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String imageUrl = s3Service.upload(file);
                FeedImg feedImg = FeedImg.builder()
                        .feed(createdFeed)
                        .image_url(imageUrl)
                        .build();
                createdFeed.getFeedImgs().add(feedImg);
            }
        }

        // 해시태그 처리
        if (feedDTO.getHashtags() != null && !feedDTO.getHashtags().isEmpty()) {
            for (String tag : feedDTO.getHashtags()) {
                if (!tag.isEmpty()) {
                    FeedHashTag feedHashTag = FeedHashTag.builder()
                            .hashtag(tag)
                            .feed(createdFeed)
                            .build();
                    createdFeed.getFeedHashtags().add(feedHashTag);
                }
            }
        }

        Feed savedFeed = feedService.CreateFeed(createdFeed);
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;
        FeedResponseDTO responseDTO = new FeedResponseDTO(savedFeed, currentUserId, null);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FeedResponseDTO>> feedList(
    		@RequestParam(defaultValue = "0") int page,
    		@RequestParam(defaultValue = "5") int size,
    		@RequestParam(defaultValue = "CREATED_DATE") FeedSortType sortType
    		) {
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (CustomException e) {
        }
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;
        
        Page<Feed> resultPage;
        if (sortType == FeedSortType.HEART) {
            resultPage = feedRepository.findAllOrderByHeartCountDesc(PageRequest.of(page, size));
        } else {
            resultPage = feedRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate")));
        }

        List<Feed> feedList = resultPage.getContent();
        
        List<FeedResponseDTO> response = feedList.stream()
                .map(feed -> new FeedResponseDTO(feed, currentUserId, null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/feed/{feedId}")
    public ResponseEntity<FeedResponseDTO> getFeed(@PathVariable Long feedId) {
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (CustomException e) {
        }
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        Feed feed = feedService.GetFeedById(feedId);
        String profileUrl = userImgRepository.findByUserId(feed.getUser().getId()).get().getPath();
        FeedResponseDTO responseDTO = new FeedResponseDTO(feed, currentUserId, profileUrl);
        if (feed.getUser().getId() == currentUser.getId())
        	responseDTO.setEditable(true);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/delete-feed/{feedId}")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long feedId) {
        feedService.DeleteFeed(feedId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/feed/{feedId}/comment")
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable Long feedId,
            @RequestBody CommentRequestDTO commentRequestDTO
    ) {
        User currentUser = userService.getCurrentUser();
        Feed feed = feedService.GetFeedById(feedId);
        Comment newComment = commentService.createComment(commentRequestDTO, feed, currentUser);

        return ResponseEntity.ok(new CommentResponseDTO(newComment));
    }


    @PostMapping("/feed/{feedId}/heart/toggle")
    public ResponseEntity<FeedResponseDTO> toggleHeart(@PathVariable Long feedId) {
        User currentUser = userService.getCurrentUser();

        heartService.toggleHeart(feedId, currentUser);

        Feed feed = feedService.GetFeedById(feedId);

        FeedResponseDTO responseDTO = new FeedResponseDTO(feed, currentUser.getId(), null);
        return ResponseEntity.ok(responseDTO);
    }
    
    @GetMapping("/mypage")
    public ResponseEntity<LoungeMyPageResponseDTO> getMyLoungePage() {

        User currentUser = userService.getCurrentUser(); 

        long followerCount = followRepository.countByFollowing(currentUser);
        long followingCount = followRepository.countByFollower(currentUser);

        List<Feed> myFeeds = feedRepository.findByUserIdOrderByCreatedDateDesc(currentUser.getId());
        List<FeedResponseDTO> feedDTOs = myFeeds.stream()
                .map(feed -> new FeedResponseDTO(feed, currentUser.getId(), null))
                .collect(Collectors.toList());

        String url = userImgRepository.findByUserId(currentUser.getId()).get().getPath();
        
        if (currentUser.getIntro() == null) 
        	currentUser.setIntro("안녕하세요."); 

        // 응답 DTO 생성
        LoungeMyPageResponseDTO response = LoungeMyPageResponseDTO.builder()
                .userId(currentUser.getId())
                .nickname(currentUser.getNickname())
                .profileImageUrl(url) 
                .intro(currentUser.getIntro())                       
                .followerCount(followerCount)
                .followingCount(followingCount)
                .feedList(feedDTOs)
                .build();

        return ResponseEntity.ok(response);
    }
    
    @PostMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LoungeMyPageResponseDTO> updateProfile (
    		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "intro", required = false) String intro) {
    	User currentUser = userService.getCurrentUser();
        long followerCount = followRepository.countByFollowing(currentUser);
        long followingCount = followRepository.countByFollower(currentUser);
    	String updatedImageUrl = null;

        if (profileImage != null && !profileImage.isEmpty()) {
        	updatedImageUrl = s3Service.upload(profileImage);
            Optional<UserImg> optionalUserImg = userImgRepository.findByUserId(currentUser.getId());
            UserImg userImg;
            if (optionalUserImg.isPresent()) {
                userImg = optionalUserImg.get();
                userImg.setPath(updatedImageUrl);
            } else {
                userImg = UserImg.builder()
                        .user(currentUser)
                        .path(updatedImageUrl)
                        .build();
            }
        }
        
        if (updatedImageUrl == null) {
            Optional<UserImg> optionalUserImg = userImgRepository.findByUserId(currentUser.getId());
            if (optionalUserImg.isPresent()) {
                updatedImageUrl = optionalUserImg.get().getPath();
            } else {
                updatedImageUrl = "https://localfitbucket.s3.ap-northeast-2.amazonaws.com/default_profile.png";
            }
        }

        // 소개말 어떻게할지..
        if (intro != null) {
            currentUser.setIntro(intro);
        }

        // 업데이트된 사용자 정보 저장
        userRepository.save(currentUser);
        


        // 변경된 정보를 반영하기 위해 현재 사용자의 피드 목록도 함께 조회
        List<Feed> myFeeds = feedRepository.findByUserIdOrderByCreatedDateDesc(currentUser.getId());
        List<FeedResponseDTO> feedDTOs = myFeeds.stream()
                .map(feed -> new FeedResponseDTO(feed, currentUser.getId(), null))
                .collect(Collectors.toList());

        LoungeMyPageResponseDTO response = LoungeMyPageResponseDTO.builder()
                .userId(currentUser.getId())
                .nickname(currentUser.getNickname())
                .profileImageUrl(updatedImageUrl)
                .intro(currentUser.getIntro())	
                .followerCount(followerCount)  // TODO: 나중에 구현
                .followingCount(followingCount) // TODO: 나ㅣ중에 구현
                .feedList(feedDTOs)
                .build();

        return ResponseEntity.ok(response);
    }
    
    @PostMapping(value = "/update-feed/{feedId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FeedResponseDTO> updateFeed(
            @PathVariable Long feedId,
            @RequestPart("feedData") FeedRequestDTO feedDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles
    ) {
        Feed updatedFeed = feedService.updateFeed(feedId, feedDTO, imageFiles);
        Long currentUserId = userService.getCurrentUser().getId();
        FeedResponseDTO responseDTO = new FeedResponseDTO(updatedFeed, currentUserId, null);
        return ResponseEntity.ok(responseDTO);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<LoungeMyPageResponseDTO> getUserLoungePage(@PathVariable Long userId) {
        User user = userRepository.findById(userId).get();

        User currentUser = userService.getCurrentUser();
        
        boolean isFollowing = false;
        if (currentUser != null && !currentUser.getId().equals(user.getId())) {
            isFollowing = followRepository.findByFollowerAndFollowing(currentUser, user).isPresent();
        }
        System.out.println(isFollowing);
        
        long followerCount = followRepository.countByFollowing(user);
        long followingCount = followRepository.countByFollower(user);
        
        List<Feed> userFeeds = feedRepository.findByUserIdOrderByCreatedDateDesc(userId);
        List<FeedResponseDTO> feedDTOs = userFeeds.stream()
            .map(feed -> {
                return new FeedResponseDTO(feed, null, null);
            })
            .collect(Collectors.toList());

        String profileImageUrl = userImgRepository.findByUserId(userId)
            .map(UserImg::getPath)
            .orElse("https://localfitbucket.s3.ap-northeast-2.amazonaws.com/default_profile.png");

        if (user.getIntro() == null) {
            user.setIntro("안녕하세요.");
        }

        LoungeMyPageResponseDTO response = LoungeMyPageResponseDTO.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImageUrl(profileImageUrl)
            .intro(user.getIntro())
            .followerCount(followerCount)    
            .followingCount(followingCount)   
            .feedList(feedDTOs)
            .isFollowing(isFollowing)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/list/main")
    public List<FeedResponseDTO> mainFeedList() {
        Page<Feed> resultPage = feedRepository.findAllOrderByHeartCountDesc(PageRequest.of(0, 4));

        List<Feed> feedList = resultPage.getContent();
        // currentUserId 사용하지 않음
        List<FeedResponseDTO> response = feedList.stream()
                .map(feed -> new FeedResponseDTO(feed, 1L, null))
                .collect(Collectors.toList());
        return response;
    }
}
