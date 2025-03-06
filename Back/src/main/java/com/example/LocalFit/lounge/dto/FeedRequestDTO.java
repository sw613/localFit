package com.example.LocalFit.lounge.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedRequestDTO {
	private String description;
	private List<String> hashtags;
	private List<String> existingImages;
}
