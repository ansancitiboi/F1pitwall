package com.ansancitiboi.f1.domain.race.service;

import com.ansancitiboi.f1.domain.race.dto.*;
import com.ansancitiboi.f1.domain.race.entity.RaceStatus;
import com.ansancitiboi.f1.domain.race.repository.PitStopRepository;
import com.ansancitiboi.f1.domain.race.repository.RaceRepository;
import com.ansancitiboi.f1.domain.race.repository.RaceResultRepository;
import com.ansancitiboi.f1.global.common.exception.BusinessException;
import com.ansancitiboi.f1.global.common.exception.ErrorCode;
import com.ansancitiboi.f1.infra.openf1.OpenF1Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RaceService {

    private final RaceRepository raceRepository;
    private final RaceResultRepository raceResultRepository;
    private final PitStopRepository pitStopRepository;
    private final OpenF1Client openF1Client;

    public List<RaceResponse> findBySeason(Integer season) {
        int targetSeason = season != null ? season : LocalDate.now().getYear();
        return raceRepository.findBySeasonOrderByRoundAsc(targetSeason).stream()
                .map(RaceResponse::from)
                .toList();
    }

    public RaceResponse findById(Long raceId) {
        return raceRepository.findById(raceId)
                .map(RaceResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.RACE_NOT_FOUND));
    }

    public List<RaceResultResponse> findResults(Long raceId) {
        if (!raceRepository.existsById(raceId)) {
            throw new BusinessException(ErrorCode.RACE_NOT_FOUND);
        }
        return raceResultRepository.findByRaceIdOrderByPositionAsc(raceId).stream()
                .map(RaceResultResponse::from)
                .toList();
    }

    public List<PitStopResponse> findPitStops(Long raceId) {
        if (!raceRepository.existsById(raceId)) {
            throw new BusinessException(ErrorCode.RACE_NOT_FOUND);
        }
        return pitStopRepository.findByRaceIdOrderByLapAsc(raceId).stream()
                .map(PitStopResponse::from)
                .toList();
    }

    public List<LivePositionResponse> findLivePositions() {
        return raceRepository.findByStatus(RaceStatus.IN_PROGRESS)
                .filter(race -> race.getSessionKey() != null)
                .map(race -> {
                    List<LivePositionResponse> positions = openF1Client
                            .getPositions(race.getSessionKey()).stream()
                            .collect(Collectors.toMap(
                                    p -> p.driverNumber(),
                                    p -> p,
                                    (a, b) -> b
                            ))
                            .values().stream()
                            .sorted((a, b) -> Integer.compare(
                                    a.position() != null ? a.position() : 99,
                                    b.position() != null ? b.position() : 99))
                            .map(LivePositionResponse::from)
                            .toList();
                    return positions;
                })
                .orElse(List.of());
    }
}
