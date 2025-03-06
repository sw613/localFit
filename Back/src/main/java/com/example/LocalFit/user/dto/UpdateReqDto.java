package com.example.LocalFit.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UpdateReqDto {

    @NotBlank
    @Size(max = 20, message = "20자 이하로 작성해 주세요")
    private String nickname;

    @NotBlank
    private String birth;

    @NotBlank
    private String gender;
}
