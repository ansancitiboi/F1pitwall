package com.ansancitiboi.f1.domain.user;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import com.ansancitiboi.f1.domain.driver.repository.DriverRepository;
import com.ansancitiboi.f1.domain.user.dto.SignupRequest;
import com.ansancitiboi.f1.domain.user.entity.User;
import com.ansancitiboi.f1.domain.user.repository.UserRepository;
import com.ansancitiboi.f1.domain.user.service.AuthService;
import com.ansancitiboi.f1.domain.user.service.UserService;
import com.ansancitiboi.f1.global.common.exception.BusinessException;
import com.ansancitiboi.f1.global.common.exception.ErrorCode;
import com.ansancitiboi.f1.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class UserServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired UserService userService;
    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired DriverRepository driverRepository;

    User user;
    Driver driver;

    @BeforeEach
    void setUp() {
        authService.signup(new SignupRequest("sub@test.com", "password123", "구독유저"));
        user = userRepository.findByEmail("sub@test.com").get();

        driver = driverRepository.findByDriverNumber(1)
                .orElseGet(() -> driverRepository.save(Driver.builder()
                        .driverNumber(1)
                        .code("VER")
                        .firstName("Max")
                        .lastName("Verstappen")
                        .teamName("Red Bull Racing")
                        .nationality("Dutch")
                        .headshotUrl("")
                        .build()));
    }

    @Test
    @DisplayName("드라이버 구독 성공")
    void subscribe_success() {
        userService.subscribe(user.getId(), driver.getId());

        assertThat(userService.getMyDrivers(user.getId()))
                .hasSize(1)
                .extracting("code")
                .containsExactly("VER");
    }

    @Test
    @DisplayName("이미 구독 중인 드라이버 재구독 시 예외 발생")
    void subscribe_duplicate_throwsException() {
        userService.subscribe(user.getId(), driver.getId());

        assertThatThrownBy(() -> userService.subscribe(user.getId(), driver.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_SUBSCRIBED);
    }

    @Test
    @DisplayName("구독 해제 성공")
    void unsubscribe_success() {
        userService.subscribe(user.getId(), driver.getId());
        userService.unsubscribe(user.getId(), driver.getId());

        assertThat(userService.getMyDrivers(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("구독하지 않은 드라이버 해제 시 예외 발생")
    void unsubscribe_notSubscribed_throwsException() {
        assertThatThrownBy(() -> userService.unsubscribe(user.getId(), driver.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("내 정보 조회")
    void getMe_success() {
        var response = userService.getMe(user.getId());

        assertThat(response.email()).isEqualTo("sub@test.com");
        assertThat(response.nickname()).isEqualTo("구독유저");
    }
}
