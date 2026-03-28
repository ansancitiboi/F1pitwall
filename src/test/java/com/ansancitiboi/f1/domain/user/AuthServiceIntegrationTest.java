package com.ansancitiboi.f1.domain.user;

import com.ansancitiboi.f1.domain.user.dto.LoginRequest;
import com.ansancitiboi.f1.domain.user.dto.SignupRequest;
import com.ansancitiboi.f1.domain.user.dto.TokenResponse;
import com.ansancitiboi.f1.domain.user.repository.UserRepository;
import com.ansancitiboi.f1.domain.user.service.AuthService;
import com.ansancitiboi.f1.global.common.exception.BusinessException;
import com.ansancitiboi.f1.global.common.exception.ErrorCode;
import com.ansancitiboi.f1.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class AuthServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        authService.signup(new SignupRequest("test@test.com", "password123", "테스트"));

        assertThat(userRepository.existsByEmail("test@test.com")).isTrue();
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 예외 발생")
    void signup_duplicateEmail_throwsException() {
        authService.signup(new SignupRequest("dup@test.com", "password123", "유저1"));

        assertThatThrownBy(() ->
                authService.signup(new SignupRequest("dup@test.com", "password456", "유저2"))
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("로그인 성공 시 토큰 반환")
    void login_success() {
        authService.signup(new SignupRequest("login@test.com", "password123", "로그인유저"));

        TokenResponse token = authService.login(new LoginRequest("login@test.com", "password123"));

        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
    void login_wrongPassword_throwsException() {
        authService.signup(new SignupRequest("wrong@test.com", "password123", "유저"));

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("wrong@test.com", "wrongpassword"))
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("Refresh Token으로 Access Token 재발급")
    void reissue_success() {
        authService.signup(new SignupRequest("reissue@test.com", "password123", "재발급유저"));
        TokenResponse token = authService.login(new LoginRequest("reissue@test.com", "password123"));

        TokenResponse newToken = authService.reissue(token.refreshToken());

        assertThat(newToken.accessToken()).isNotBlank();
        assertThat(newToken.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("로그아웃 후 Refresh Token 재사용 불가")
    void logout_thenReissue_throwsException() {
        authService.signup(new SignupRequest("logout@test.com", "password123", "로그아웃유저"));
        TokenResponse token = authService.login(new LoginRequest("logout@test.com", "password123"));

        Long userId = userRepository.findByEmail("logout@test.com").get().getId();
        authService.logout(userId);

        assertThatThrownBy(() -> authService.reissue(token.refreshToken()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }
}
