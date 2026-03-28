package com.ansancitiboi.f1.domain.race.dto;

import com.ansancitiboi.f1.domain.race.entity.PitStop;

public record PitStopResponse(
        Integer lap,
        Integer stop,
        String duration,
        Long driverId,
        String driverCode,
        String driverFirstName,
        String driverLastName
) {
    public static PitStopResponse from(PitStop pitStop) {
        return new PitStopResponse(
                pitStop.getLap(),
                pitStop.getStop(),
                pitStop.getDuration(),
                pitStop.getDriver().getId(),
                pitStop.getDriver().getCode(),
                pitStop.getDriver().getFirstName(),
                pitStop.getDriver().getLastName()
        );
    }
}
