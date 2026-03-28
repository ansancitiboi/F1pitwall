package com.ansancitiboi.f1.domain.race.controller;

import com.ansancitiboi.f1.domain.race.dto.*;
import com.ansancitiboi.f1.domain.race.service.RaceService;
import com.ansancitiboi.f1.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/races")
@RequiredArgsConstructor
public class RaceController {

    private final RaceService raceService;

    @GetMapping
    public ApiResponse<List<RaceResponse>> getRaces(
            @RequestParam(required = false) Integer season) {
        return ApiResponse.ok(raceService.findBySeason(season));
    }

    @GetMapping("/{raceId}")
    public ApiResponse<RaceResponse> getRace(@PathVariable Long raceId) {
        return ApiResponse.ok(raceService.findById(raceId));
    }

    @GetMapping("/{raceId}/results")
    public ApiResponse<List<RaceResultResponse>> getRaceResults(@PathVariable Long raceId) {
        return ApiResponse.ok(raceService.findResults(raceId));
    }

    @GetMapping("/{raceId}/pitstops")
    public ApiResponse<List<PitStopResponse>> getPitStops(@PathVariable Long raceId) {
        return ApiResponse.ok(raceService.findPitStops(raceId));
    }

    @GetMapping("/live")
    public ApiResponse<List<LivePositionResponse>> getLivePositions() {
        return ApiResponse.ok(raceService.findLivePositions());
    }
}
