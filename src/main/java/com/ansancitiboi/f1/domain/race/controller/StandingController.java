package com.ansancitiboi.f1.domain.race.controller;

import com.ansancitiboi.f1.domain.race.dto.DriverStandingResponse;
import com.ansancitiboi.f1.domain.race.service.StandingService;
import com.ansancitiboi.f1.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/standings")
@RequiredArgsConstructor
public class StandingController {

    private final StandingService standingService;

    @GetMapping("/drivers")
    public ApiResponse<List<DriverStandingResponse>> getDriverStandings(
            @RequestParam(required = false) Integer season) {
        return ApiResponse.ok(standingService.getDriverStandings(season));
    }
}
