package com.example.LocalFit.user.dto;

import com.example.LocalFit.auth.AuthProvider;
import com.example.LocalFit.user.entity.Role;
import com.example.LocalFit.user.entity.User;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserResDto {

    private Long id;

    private String name;

    private String nickname;

    private String birth;

    private String gender;

    private String email;

    private AuthProvider provider;

    private Role role;

    public static UserResDto from(User user) {
        return UserResDto.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .birth(user.getBirth())
                .gender(user.getGender())
                .email(user.getEmail())
                .provider(user.getProvider())
                .role(user.getRole())
                .build();
    }
}
