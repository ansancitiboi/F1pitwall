package com.ansancitiboi.f1.domain.race.batch;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import com.ansancitiboi.f1.domain.driver.repository.DriverRepository;
import com.ansancitiboi.f1.domain.race.entity.Race;
import com.ansancitiboi.f1.domain.race.entity.RaceResult;
import com.ansancitiboi.f1.domain.race.entity.RaceStatus;
import com.ansancitiboi.f1.domain.race.repository.RaceRepository;
import com.ansancitiboi.f1.domain.race.repository.RaceResultRepository;
import com.ansancitiboi.f1.infra.openf1.OpenF1Client;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1PositionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RaceResultCollectJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RaceRepository raceRepository;
    private final RaceResultRepository raceResultRepository;
    private final DriverRepository driverRepository;
    private final OpenF1Client openF1Client;

    // F1 포인트 시스템
    private static final Map<Integer, BigDecimal> POINTS_MAP = Map.of(
            1, BigDecimal.valueOf(25),
            2, BigDecimal.valueOf(18),
            3, BigDecimal.valueOf(15),
            4, BigDecimal.valueOf(12),
            5, BigDecimal.valueOf(10),
            6, BigDecimal.valueOf(8),
            7, BigDecimal.valueOf(6),
            8, BigDecimal.valueOf(4),
            9, BigDecimal.valueOf(2),
            10, BigDecimal.valueOf(1)
    );

    @Bean
    public Job collectRaceResultJob() {
        return new JobBuilder("collectRaceResultJob", jobRepository)
                .start(collectRaceResultStep())
                .build();
    }

    @Bean
    public Step collectRaceResultStep() {
        return new StepBuilder("collectRaceResultStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // Job 파라미터에서 raceId 읽기
                    Long raceId = chunkContext.getStepContext()
                            .getStepExecution()
                            .getJobParameters()
                            .getLong("raceId");

                    Race race = raceRepository.findById(raceId)
                            .orElseThrow(() -> new IllegalArgumentException("Race not found: " + raceId));

                    if (race.getSessionKey() == null) {
                        log.warn("sessionKey가 없는 레이스: {}", raceId);
                        return RepeatStatus.FINISHED;
                    }

                    log.info("레이스 결과 수집 시작 - raceId: {}, sessionKey: {}", raceId, race.getSessionKey());

                    // OpenF1에서 전체 포지션 데이터 조회
                    List<OpenF1PositionResponse> positions = openF1Client.getFinalPositions(race.getSessionKey());

                    if (positions == null || positions.isEmpty()) {
                        log.warn("포지션 데이터 없음 - sessionKey: {}", race.getSessionKey());
                        return RepeatStatus.FINISHED;
                    }

                    // 드라이버번호 기준으로 마지막(최종) 포지션만 남기기
                    Map<Integer, OpenF1PositionResponse> finalPositions = positions.stream()
                            .collect(Collectors.toMap(
                                    OpenF1PositionResponse::driverNumber,
                                    p -> p,
                                    (existing, replacement) -> replacement  // 나중 것으로 덮어쓰기
                            ));

                    List<RaceResult> results = new ArrayList<>();

                    for (Map.Entry<Integer, OpenF1PositionResponse> entry : finalPositions.entrySet()) {
                        Integer driverNumber = entry.getKey();
                        Integer position = entry.getValue().position();

                        Optional<Driver> driverOpt = driverRepository.findByDriverNumber(driverNumber);
                        if (driverOpt.isEmpty()) {
                            log.warn("드라이버 없음 - driverNumber: {}", driverNumber);
                            continue;
                        }

                        BigDecimal points = POINTS_MAP.getOrDefault(position, BigDecimal.ZERO);

                        results.add(RaceResult.builder()
                                .race(race)
                                .driver(driverOpt.get())
                                .position(position)
                                .points(points)
                                .fastestLap(false)
                                .resultStatus("FINISHED")
                                .build());
                    }

                    raceResultRepository.saveAll(results);
                    race.updateStatus(RaceStatus.COMPLETED);
                    raceRepository.save(race);

                    log.info("레이스 결과 수집 완료 - {}건 저장", results.size());
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
