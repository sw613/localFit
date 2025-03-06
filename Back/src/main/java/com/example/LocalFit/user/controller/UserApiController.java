package com.example.LocalFit.user.controller;

import com.example.LocalFit.user.dto.*;
import com.example.LocalFit.user.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<UserResDto> join(@Valid @RequestBody JoinReqDto joinReqDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.join(joinReqDto));
    }

    @PutMapping("/additional-info")
    public ResponseEntity<UserResDto> additionalInfo(@Valid @RequestBody AdditionalInfoReqDto reqDto) {
        UserResDto result = userService.additionalInfo(reqDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResDto> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UpdateReqDto updateReqDto) {
        return ResponseEntity.ok(userService.updateUser(id,updateReqDto));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdatePasswordReqDto request
    ) {
        userService.updatePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id, @RequestBody(required = false) DeleteUserReqDto deleteUserReqDto) {
        userService.deleteUser(id, deleteUserReqDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        return ResponseEntity.ok(userService.checkEmailDuplicate(email));
    }

    @GetMapping("/check/nickname")
    public ResponseEntity<Boolean> checkNicknameDuplicate(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.checkNicknameDuplicate(nickname));
    }

    @GetMapping("/mypage")
    public ResponseEntity<UserResDto> getMyPage() {
        return ResponseEntity.ok(userService.getMyPage());
    }
    
	@GetMapping("/curUser")
	public ResponseEntity<UserResDto> getCurrentUserDto() {
		return ResponseEntity.ok(userService.getCurrentUserDto());
	}
}
