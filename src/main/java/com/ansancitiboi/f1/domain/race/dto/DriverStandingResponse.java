package com.ansancitiboi.f1.domain.race.dto;

import com.ansancitiboi.f1.domain.race.entity.DriverStanding;

import java.math.BigDecimal;

public record DriverStandingResponse(
        Integer position,
        BigDecimal points,
        Integer wins,
        Long driverId,
        String driverCode,
        String driverFirstName,
        String driverLastName,
        String teamName
) {
    public static DriverStandingResponse from(DriverStanding standing) {
        return new DriverStandingResponse(
                standing.getPosition(),
                standing.getPoints(),
                standing.getWins(),
                standing.getDriver().getId(),
                standing.getDriver().getCode(),
                standing.getDriver().getFirstName(),
                standing.getDriver().getLastName(),
                standing.getDriver().getTeamName()
        );
    }
}
