package com.example.LocalFit.connectHashtag.controller;

import com.example.LocalFit.connectHashtag.entity.ConnectHashtagRequestDto;
import com.example.LocalFit.connectHashtag.entity.ConnectHashtagResponseDto;
import com.example.LocalFit.connectHashtag.service.ConnectHashtagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/connectHashtag")
@RequiredArgsConstructor
public class ConnectHashtagApiController {

    private final ConnectHashtagService connectHashtagService;

    // 모임 개설시 호출
    @PostMapping("/create")
    public ConnectHashtagResponseDto createConnectHashtag(ConnectHashtagRequestDto connectHashtagRequestDto) {
        return null;
    }
}
