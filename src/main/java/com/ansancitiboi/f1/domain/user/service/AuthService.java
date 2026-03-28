package com.ansancitiboi.f1.domain.user.service;

import com.ansancitiboi.f1.domain.user.dto.LoginRequest;
import com.ansancitiboi.f1.domain.user.dto.SignupRequest;
import com.ansancitiboi.f1.domain.user.dto.TokenResponse;
import com.ansancitiboi.f1.domain.user.entity.User;
import com.ansancitiboi.f1.domain.user.repository.UserRepository;
import com.ansancitiboi.f1.global.auth.jwt.JwtProvider;
import com.ansancitiboi.f1.global.common.exception.BusinessException;
import com.ansancitiboi.f1.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getEmail());

        // Redis에 Refresh Token 저장 (7일)
        redisTemplate.opsForValue().set(
                "refresh:" + user.getId(),
                refreshToken,
                Duration.ofDays(7)
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    public void logout(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        String stored = redisTemplate.opsForValue().get("refresh:" + userId);

        if (stored == null || !stored.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId(), user.getEmail());

        redisTemplate.opsForValue().set(
                "refresh:" + userId,
                newRefreshToken,
                Duration.ofDays(7)
        );

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
