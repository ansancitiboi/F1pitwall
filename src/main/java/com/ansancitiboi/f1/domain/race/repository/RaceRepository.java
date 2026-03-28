package com.ansancitiboi.f1.domain.race.repository;

import com.ansancitiboi.f1.domain.race.entity.Race;
import com.ansancitiboi.f1.domain.race.entity.RaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RaceRepository extends JpaRepository<Race, Long> {

    List<Race> findBySeasonOrderByRoundAsc(Integer season);

    Optional<Race> findByStatus(RaceStatus status);

    boolean existsBySeasonAndRound(Integer season, Integer round);
}
