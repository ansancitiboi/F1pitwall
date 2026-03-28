package com.ansancitiboi.f1.domain.driver.controller;

import com.ansancitiboi.f1.domain.driver.dto.DriverResponse;
import com.ansancitiboi.f1.domain.driver.service.DriverService;
import com.ansancitiboi.f1.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    public ApiResponse<List<DriverResponse>> getDrivers() {
        return ApiResponse.ok(driverService.findAll());
    }

    @GetMapping("/{driverId}")
    public ApiResponse<DriverResponse> getDriver(@PathVariable Long driverId) {
        return ApiResponse.ok(driverService.findById(driverId));
    }
}
