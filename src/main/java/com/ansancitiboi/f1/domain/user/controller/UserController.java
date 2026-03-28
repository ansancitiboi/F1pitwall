package com.ansancitiboi.f1.domain.user.controller;

import com.ansancitiboi.f1.domain.driver.dto.DriverResponse;
import com.ansancitiboi.f1.domain.user.dto.UserResponse;
import com.ansancitiboi.f1.domain.user.service.UserService;
import com.ansancitiboi.f1.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userService.getMe(userId));
    }

    @GetMapping("/me/drivers")
    public ApiResponse<List<DriverResponse>> getMyDrivers(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userService.getMyDrivers(userId));
    }

    @PostMapping("/me/drivers/{driverId}")
    public ApiResponse<Void> subscribe(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long driverId) {
        userService.subscribe(userId, driverId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/me/drivers/{driverId}")
    public ApiResponse<Void> unsubscribe(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long driverId) {
        userService.unsubscribe(userId, driverId);
        return ApiResponse.ok(null);
    }
}
