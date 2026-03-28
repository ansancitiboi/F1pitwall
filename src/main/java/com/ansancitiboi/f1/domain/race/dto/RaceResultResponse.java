package com.ansancitiboi.f1.domain.race.dto;

import com.ansancitiboi.f1.domain.race.entity.RaceResult;

import java.math.BigDecimal;

public record RaceResultResponse(
        Integer position,
        BigDecimal points,
        Boolean fastestLap,
        String resultStatus,
        Long driverId,
        String driverCode,
        String driverFirstName,
        String driverLastName,
        String teamName
) {
    public static RaceResultResponse from(RaceResult result) {
        return new RaceResultResponse(
                result.getPosition(),
                result.getPoints(),
                result.getFastestLap(),
                result.getResultStatus(),
                result.getDriver().getId(),
                result.getDriver().getCode(),
                result.getDriver().getFirstName(),
                result.getDriver().getLastName(),
                result.getDriver().getTeamName()
        );
    }
}
