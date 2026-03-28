package com.ansancitiboi.f1.domain.race.service;

import com.ansancitiboi.f1.domain.race.dto.DriverStandingResponse;
import com.ansancitiboi.f1.domain.race.repository.DriverStandingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StandingService {

    private final DriverStandingRepository driverStandingRepository;

    public List<DriverStandingResponse> getDriverStandings(Integer season) {
        int targetSeason = season != null ? season : LocalDate.now().getYear();
        return driverStandingRepository.findBySeasonOrderByPositionAsc(targetSeason).stream()
                .map(DriverStandingResponse::from)
                .toList();
    }
}
