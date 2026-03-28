package com.ansancitiboi.f1.domain.race.batch;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import com.ansancitiboi.f1.domain.driver.repository.DriverRepository;
import com.ansancitiboi.f1.domain.race.entity.PitStop;
import com.ansancitiboi.f1.domain.race.entity.Race;
import com.ansancitiboi.f1.domain.race.repository.PitStopRepository;
import com.ansancitiboi.f1.domain.race.repository.RaceRepository;
import com.ansancitiboi.f1.infra.openf1.OpenF1Client;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1PitResponse;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PitStopCollectJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RaceRepository raceRepository;
    private final PitStopRepository pitStopRepository;
    private final DriverRepository driverRepository;
    private final OpenF1Client openF1Client;

    @Bean
    public Job collectPitStopJob() {
        return new JobBuilder("collectPitStopJob", jobRepository)
                .start(collectPitStopStep())
                .build();
    }

    @Bean
    public Step collectPitStopStep() {
        return new StepBuilder("collectPitStopStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
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

                    log.info("피트스톱 수집 시작 - raceId: {}, sessionKey: {}", raceId, race.getSessionKey());

                    List<OpenF1PitResponse> pitData = openF1Client.getPitStops(race.getSessionKey());

                    if (pitData == null || pitData.isEmpty()) {
                        log.warn("피트스톱 데이터 없음 - sessionKey: {}", race.getSessionKey());
                        return RepeatStatus.FINISHED;
                    }

                    // 드라이버번호별 피트스톱 횟수 카운트
                    Map<Integer, Integer> stopCountMap = new HashMap<>();
                    List<PitStop> pitStops = new ArrayList<>();

                    for (OpenF1PitResponse pit : pitData) {
                        Optional<Driver> driverOpt = driverRepository.findByDriverNumber(pit.driverNumber());
                        if (driverOpt.isEmpty()) continue;

                        int stopCount = stopCountMap.merge(pit.driverNumber(), 1, Integer::sum);

                        String duration = pit.pitDuration() != null
                                ? String.format("%.3f", pit.pitDuration())
                                : "-";

                        pitStops.add(PitStop.builder()
                                .race(race)
                                .driver(driverOpt.get())
                                .lap(pit.lapNumber())
                                .stop(stopCount)
                                .duration(duration)
                                .build());
                    }

                    pitStopRepository.saveAll(pitStops);
                    log.info("피트스톱 수집 완료 - {}건 저장", pitStops.size());
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
