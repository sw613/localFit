package com.example.LocalFit.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdatePasswordReqDto {
    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 16, message = "8자 이상 16자 이하로 작성해 주세요")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.* ).{8,16}$"
            , message = "8~16자리의 영대소문자, 특수문자, 숫자를 포함해주세요")
    private String newPassword;
}
