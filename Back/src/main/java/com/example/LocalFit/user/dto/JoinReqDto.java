package com.example.LocalFit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class JoinReqDto {
    @NotBlank
    @Size(max = 25, message = "25자 이하로 작성해 주세요")
    private String name;

    @NotBlank
    @Size(max = 20, message = "20자 이하로 작성해 주세요")
    private String nickname;

    @NotBlank
    private String birth;

    @NotBlank
    private String gender;

    @NotBlank
    @Email
    @Size(max = 50, message = "50자 이하로 작성해 주세요")
    @Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+(\\.[a-z]+)+$",
            message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank
    @Size(min = 8, max = 16)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.* ).{8,16}$"
            , message = "8~16자리의 영대소문자, 특수문자, 숫자를 포함해주세요")
    private String password;
}
