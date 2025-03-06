package com.example.LocalFit.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdditionalInfoReqDto {

    @NotBlank(message = "닉네임을 입력해주세요")
    private String nickname;

    @NotBlank(message = "생년월일을 입력해주세요")
    private String birth;

    @NotBlank(message = "성별을 입력해주세요")
    private String gender;
}
