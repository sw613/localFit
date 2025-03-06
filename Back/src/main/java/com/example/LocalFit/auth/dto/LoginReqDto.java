package com.example.LocalFit.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginReqDto {
    @NotBlank(message = "이메일을 입력해주세요")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
