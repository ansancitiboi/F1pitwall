package com.ansancitiboi.f1.domain.driver.dto;

import com.ansancitiboi.f1.domain.driver.entity.Driver;

public record DriverResponse(
        Long id,
        Integer driverNumber,
        String code,
        String firstName,
        String lastName,
        String teamName,
        String nationality,
        String headshotUrl
) {
    public static DriverResponse from(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getDriverNumber(),
                driver.getCode(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getTeamName(),
                driver.getNationality(),
                driver.getHeadshotUrl()
        );
    }
}
