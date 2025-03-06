package com.example.LocalFit.auth.oauth2.info;

import com.example.LocalFit.auth.AuthProvider;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;

import java.util.Map;

public class OAuth2InfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
            return new GoogleUserInfo(attributes);
        }
//        else if (registrationId.equalsIgnoreCase(AuthProvider.KAKAO.toString())) {
//            return new KakaoUserInfo(attributes);
//        }
        throw new CustomException(CustomErrorCode.INVALID_OAUTH_PROVIDER);
    }
}
