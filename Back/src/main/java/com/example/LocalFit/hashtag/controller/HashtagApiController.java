package com.example.LocalFit.hashtag.controller;

import com.example.LocalFit.category.entity.CategoryRequestDto;
import com.example.LocalFit.hashtag.entity.HashtagRequestDto;
import com.example.LocalFit.hashtag.entity.HashtagResponseDto;
import com.example.LocalFit.hashtag.service.HashtagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hashtag")
@RequiredArgsConstructor
public class HashtagApiController {

    private final HashtagService hashtagService;

    @PostMapping("/create")
    public HashtagResponseDto createCategory(@RequestBody HashtagRequestDto hashtagRequestDto) {
        return null;
    }

}
