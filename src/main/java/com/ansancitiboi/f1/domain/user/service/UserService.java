package com.ansancitiboi.f1.domain.user.service;

import com.ansancitiboi.f1.domain.driver.dto.DriverResponse;
import com.ansancitiboi.f1.domain.driver.entity.Driver;
import com.ansancitiboi.f1.domain.driver.entity.UserDriverSubscription;
import com.ansancitiboi.f1.domain.driver.repository.DriverRepository;
import com.ansancitiboi.f1.domain.driver.repository.UserDriverSubscriptionRepository;
import com.ansancitiboi.f1.domain.user.dto.UserResponse;
import com.ansancitiboi.f1.domain.user.entity.User;
import com.ansancitiboi.f1.domain.user.repository.UserRepository;
import com.ansancitiboi.f1.global.common.exception.BusinessException;
import com.ansancitiboi.f1.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final UserDriverSubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getMyDrivers(Long userId) {
        return subscriptionRepository.findDriversByUserId(userId).stream()
                .map(DriverResponse::from)
                .toList();
    }

    @Transactional
    public void subscribe(Long userId, Long driverId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRIVER_NOT_FOUND));

        if (subscriptionRepository.existsByUserAndDriver(user, driver)) {
            throw new BusinessException(ErrorCode.ALREADY_SUBSCRIBED);
        }

        subscriptionRepository.save(UserDriverSubscription.builder()
                .user(user)
                .driver(driver)
                .build());
    }

    @Transactional
    public void unsubscribe(Long userId, Long driverId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRIVER_NOT_FOUND));

        UserDriverSubscription subscription = subscriptionRepository.findByUserAndDriver(user, driver)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscriptionRepository.delete(subscription);
    }
}
