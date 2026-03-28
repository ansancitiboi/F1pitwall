package com.ansancitiboi.f1.domain.race.repository;

import com.ansancitiboi.f1.domain.race.entity.RaceResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceResultRepository extends JpaRepository<RaceResult, Long> {

    @EntityGraph(attributePaths = {"driver"})
    List<RaceResult> findByRaceIdOrderByPositionAsc(Long raceId);

    @EntityGraph(attributePaths = {"driver", "race"})
    List<RaceResult> findByDriverIdOrderByRaceSeasonDescRaceRoundDesc(Long driverId);
}
