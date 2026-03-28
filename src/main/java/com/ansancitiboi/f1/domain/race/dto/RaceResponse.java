package com.ansancitiboi.f1.domain.race.dto;

import com.ansancitiboi.f1.domain.race.entity.Race;
import com.ansancitiboi.f1.domain.race.entity.RaceStatus;

import java.time.LocalDate;

public record RaceResponse(
        Long id,
        Integer season,
        Integer round,
        String raceName,
        String circuit,
        String country,
        LocalDate raceDate,
        RaceStatus status
) {
    public static RaceResponse from(Race race) {
        return new RaceResponse(
                race.getId(),
                race.getSeason(),
                race.getRound(),
                race.getRaceName(),
                race.getCircuit(),
                race.getCountry(),
                race.getRaceDate(),
                race.getStatus()
        );
    }
}
