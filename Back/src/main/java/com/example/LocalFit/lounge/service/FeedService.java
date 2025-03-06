package com.example.LocalFit.lounge.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.LocalFit.global.S3Service;
import com.example.LocalFit.lounge.dto.FeedRequestDTO;
import com.example.LocalFit.lounge.entity.Feed;
import com.example.LocalFit.lounge.entity.FeedHashTag;
import com.example.LocalFit.lounge.entity.FeedImg;
import com.example.LocalFit.lounge.repository.FeedRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FeedService {
	private final FeedRepository feedRepository;
    private final S3Service s3Service;
    
	public Feed CreateFeed(Feed feed) {
		return feedRepository.save(feed);
	}
	
	public List<Feed> GetFeedList() {
		return feedRepository.findAll();
	}
	
    public Feed GetFeedById(Long feedId) {
        return feedRepository.findById(feedId).orElse(null);
    }
    
    public Feed updateFeed(Long feedId, FeedRequestDTO feedDTO, List<MultipartFile> imageFiles) {
        // 피드 조회
        Feed feed = GetFeedById(feedId);
        if (feed == null) {
            throw new EntityNotFoundException("Feed not found with id " + feedId);
        }

        feed.setDescription(feedDTO.getDescription());

        feed.getFeedHashtags().clear();
        if (feedDTO.getHashtags() != null) {
            for (String tag : feedDTO.getHashtags()) {
                if (!tag.isEmpty()) {
                    FeedHashTag feedHashTag = FeedHashTag.builder()
                            .hashtag(tag)
                            .feed(feed)
                            .build();
                    feed.getFeedHashtags().add(feedHashTag);
                }
            }
        }
        
        List<String> existingImages = feedDTO.getExistingImages();
        if (existingImages != null) {
            feed.getFeedImgs().removeIf(feedImg -> !existingImages.contains(feedImg.getImage_url()));
        } else {
            feed.getFeedImgs().clear();
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String imageUrl = s3Service.upload(file);
                FeedImg feedImg = FeedImg.builder()
                        .feed(feed)
                        .image_url(imageUrl)
                        .build();
                feed.getFeedImgs().add(feedImg);
            }
        }

        return feedRepository.save(feed);
    }
    
    public void DeleteFeed(Long feedId) {
        feedRepository.deleteById(feedId);
    }
    
    public List<Feed> GetFeedList(int page, int size) {
    	PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdDate").descending());
    	return feedRepository.findAll(pageRequest).getContent();
    }
	
}
