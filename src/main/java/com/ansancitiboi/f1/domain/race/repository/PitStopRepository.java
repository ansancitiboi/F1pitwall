package com.ansancitiboi.f1.domain.race.repository;

import com.ansancitiboi.f1.domain.race.entity.PitStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PitStopRepository extends JpaRepository<PitStop, Long> {

    List<PitStop> findByRaceIdOrderByLapAsc(Long raceId);
}
