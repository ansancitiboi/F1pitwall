package com.ansancitiboi.f1.domain.race.batch;

import com.ansancitiboi.f1.domain.race.entity.DriverStanding;
import com.ansancitiboi.f1.domain.race.entity.RaceResult;
import com.ansancitiboi.f1.domain.race.repository.DriverStandingRepository;
import com.ansancitiboi.f1.domain.race.repository.RaceResultRepository;
import com.ansancitiboi.f1.domain.race.repository.RaceRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DriverStandingUpdateJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RaceRepository raceRepository;
    private final RaceResultRepository raceResultRepository;
    private final DriverStandingRepository driverStandingRepository;

    @Bean
    public Job updateDriverStandingJob() {
        return new JobBuilder("updateDriverStandingJob", jobRepository)
                .start(updateDriverStandingStep())
                .build();
    }

    @Bean
    public Step updateDriverStandingStep() {
        return new StepBuilder("updateDriverStandingStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Integer season = (int) (long) chunkContext.getStepContext()
                            .getStepExecution()
                            .getJobParameters()
                            .getLong("season");

                    log.info("드라이버 챔피언십 순위 갱신 시작 - season: {}", season);

                    // 시즌 전체 레이스 결과 조회
                    List<RaceResult> allResults = raceRepository.findBySeasonOrderByRoundAsc(season)
                            .stream()
                            .flatMap(race -> raceResultRepository.findByRaceIdOrderByPositionAsc(race.getId()).stream())
                            .toList();

                    // 드라이버별 누적 포인트 집계
                    Map<Long, BigDecimal> pointsMap = allResults.stream()
                            .collect(Collectors.groupingBy(
                                    r -> r.getDriver().getId(),
                                    Collectors.reducing(BigDecimal.ZERO,
                                            r -> r.getPoints() != null ? r.getPoints() : BigDecimal.ZERO,
                                            BigDecimal::add)
                            ));

                    // 드라이버별 우승 횟수
                    Map<Long, Long> winsMap = allResults.stream()
                            .filter(r -> r.getPosition() != null && r.getPosition() == 1)
                            .collect(Collectors.groupingBy(
                                    r -> r.getDriver().getId(),
                                    Collectors.counting()
                            ));

                    // 포인트 내림차순 정렬 → 순위 부여
                    List<Map.Entry<Long, BigDecimal>> sorted = pointsMap.entrySet().stream()
                            .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                            .toList();

                    for (int i = 0; i < sorted.size(); i++) {
                        Long driverId = sorted.get(i).getKey();
                        BigDecimal points = sorted.get(i).getValue();
                        int position = i + 1;
                        int wins = winsMap.getOrDefault(driverId, 0L).intValue();

                        driverStandingRepository.findBySeasonAndDriverId(season, driverId)
                                .ifPresentOrElse(
                                        standing -> standing.update(position, points, wins),
                                        () -> {
                                            allResults.stream()
                                                    .filter(r -> r.getDriver().getId().equals(driverId))
                                                    .findFirst()
                                                    .ifPresent(r -> driverStandingRepository.save(
                                                            DriverStanding.builder()
                                                                    .season(season)
                                                                    .driver(r.getDriver())
                                                                    .position(position)
                                                                    .points(points)
                                                                    .wins(wins)
                                                                    .build()
                                                    ));
                                        }
                                );
                    }

                    log.info("드라이버 챔피언십 순위 갱신 완료 - {}명", sorted.size());
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
