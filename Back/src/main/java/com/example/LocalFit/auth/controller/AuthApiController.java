package com.example.LocalFit.auth.controller;

import com.example.LocalFit.auth.dto.LoginReqDto;
import com.example.LocalFit.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthApiController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginReqDto loginReqDto,
            HttpServletResponse response
    ) {
        authService.login(loginReqDto, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.refresh(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }
}
