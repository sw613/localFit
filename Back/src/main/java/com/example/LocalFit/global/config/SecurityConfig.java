package com.example.LocalFit.global.config;

import com.example.LocalFit.auth.jwt.JwtFilter;
import com.example.LocalFit.auth.jwt.TokenProvider;
import com.example.LocalFit.auth.oauth2.handler.OAuth2FailureHandler;
import com.example.LocalFit.auth.oauth2.handler.OAuth2SuccessHandler;
import com.example.LocalFit.auth.oauth2.service.CustomOAuth2Service;
import com.example.LocalFit.auth.service.RedisTokenService;
import com.example.LocalFit.global.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;
    private final RedisTokenService redisTokenService;
    private final CustomOAuth2Service customOAuth2Service;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/ws-stomp/**", "/api/auth/**", "/api/oauth2/**", "/api/users/join", "/api/facility/**", "/api/meeting/**", "/api/lounge/list/main","/api/v1/search/**").permitAll()
                        .requestMatchers("/auto-complete").permitAll()
                        .requestMatchers("/batch/run").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // 관리자 전용 API
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/api/auth/login")
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/api/oauth2/authorize")) //구글 로그인 리다이렉트
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/api/login/oauth2/code/*")) //구글 인증 후 리다이렉트
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2Service))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable());

        http.formLogin(login -> login.disable());

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Access-Control-Allow-Credentials"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(tokenProvider, cookieUtil, redisTokenService);
    }
}
