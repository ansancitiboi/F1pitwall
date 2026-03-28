package com.ansancitiboi.f1.domain.race.batch;

import com.ansancitiboi.f1.domain.race.entity.Race;
import com.ansancitiboi.f1.domain.race.entity.RaceStatus;
import com.ansancitiboi.f1.domain.race.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RaceBatchScheduler {

    private final JobLauncher jobLauncher;
    private final RaceResultCollectJob raceResultCollectJob;
    private final PitStopCollectJob pitStopCollectJob;
    private final DriverStandingUpdateJob driverStandingUpdateJob;
    private final RaceRepository raceRepository;

    // 매일 자정에 오늘 레이스가 완료됐는지 확인하고 배치 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void triggerRaceResultCollection() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 어제 날짜 레이스 중 IN_PROGRESS 또는 SCHEDULED 상태인 것 확인
        Optional<Race> raceOpt = raceRepository.findAll().stream()
                .filter(r -> yesterday.equals(r.getRaceDate()))
                .filter(r -> r.getStatus() != RaceStatus.COMPLETED)
                .findFirst();

        if (raceOpt.isEmpty()) {
            return;
        }

        Race race = raceOpt.get();
        log.info("레이스 배치 실행 - raceId: {}, raceName: {}", race.getId(), race.getRaceName());

        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("raceId", race.getId())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(raceResultCollectJob.collectRaceResultJob(), params);
            jobLauncher.run(pitStopCollectJob.collectPitStopJob(), params);
            jobLauncher.run(driverStandingUpdateJob.updateDriverStandingJob(),
                    new JobParametersBuilder()
                            .addLong("season", (long) race.getSeason())
                            .addLong("timestamp", System.currentTimeMillis())
                            .toJobParameters());

        } catch (Exception e) {
            log.error("배치 실행 실패 - raceId: {}", race.getId(), e);
        }
    }
}
