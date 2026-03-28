package com.ansancitiboi.f1.domain.race.dto;

import com.ansancitiboi.f1.infra.openf1.dto.OpenF1PositionResponse;

public record LivePositionResponse(
        Integer driverNumber,
        Integer position
) {
    public static LivePositionResponse from(OpenF1PositionResponse response) {
        return new LivePositionResponse(response.driverNumber(), response.position());
    }
}
