package com.ansancitiboi.f1.domain.race.repository;

import com.ansancitiboi.f1.domain.race.entity.DriverStanding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverStandingRepository extends JpaRepository<DriverStanding, Long> {

    List<DriverStanding> findBySeasonOrderByPositionAsc(Integer season);

    Optional<DriverStanding> findBySeasonAndDriverId(Integer season, Long driverId);
}
