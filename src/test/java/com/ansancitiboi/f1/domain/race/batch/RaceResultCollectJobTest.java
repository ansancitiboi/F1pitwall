package com.ansancitiboi.f1.domain.race.batch;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import com.ansancitiboi.f1.domain.driver.repository.DriverRepository;
import com.ansancitiboi.f1.domain.race.entity.Race;
import com.ansancitiboi.f1.domain.race.entity.RaceStatus;
import com.ansancitiboi.f1.domain.race.repository.RaceRepository;
import com.ansancitiboi.f1.domain.race.repository.RaceResultRepository;
import com.ansancitiboi.f1.infra.openf1.OpenF1Client;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1PositionResponse;
import com.ansancitiboi.f1.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RaceResultCollectJobTest extends AbstractIntegrationTest {

    @Autowired JobLauncher jobLauncher;
    @Autowired RaceResultCollectJob raceResultCollectJob;
    @Autowired RaceRepository raceRepository;
    @Autowired RaceResultRepository raceResultRepository;
    @Autowired DriverRepository driverRepository;

    @MockitoBean
    OpenF1Client openF1Client;

    @Test
    @DisplayName("레이스 결과 수집 Job - 정상 실행 및 RaceResult 저장 확인")
    void collectRaceResult_savesResultsAndCompletesJob() throws Exception {
        // given
        Driver driver1 = driverRepository.save(Driver.builder()
                .driverNumber(1).code("VER").firstName("Max").lastName("Verstappen")
                .teamName("Red Bull Racing").nationality("Dutch").headshotUrl("").build());
        Driver driver2 = driverRepository.save(Driver.builder()
                .driverNumber(44).code("HAM").firstName("Lewis").lastName("Hamilton")
                .teamName("Mercedes").nationality("British").headshotUrl("").build());

        Race race = raceRepository.save(Race.builder()
                .season(2025).round(1).raceName("Bahrain Grand Prix")
                .circuit("Bahrain International Circuit").country("Bahrain")
                .raceDate(LocalDate.now()).status(RaceStatus.IN_PROGRESS)
                .sessionKey(9999).build());

        when(openF1Client.getFinalPositions(anyInt())).thenReturn(List.of(
                new OpenF1PositionResponse(1, 1, "2025-03-02T15:00:00", 9999),
                new OpenF1PositionResponse(44, 2, "2025-03-02T15:00:00", 9999)
        ));

        // when
        JobParameters params = new JobParametersBuilder()
                .addLong("raceId", race.getId())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(raceResultCollectJob.collectRaceResultJob(), params);

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        var results = raceResultRepository.findByRaceIdOrderByPositionAsc(race.getId());
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getPosition()).isEqualTo(1);
        assertThat(results.get(0).getDriver().getCode()).isEqualTo("VER");
        assertThat(results.get(1).getPosition()).isEqualTo(2);

        // 레이스 상태 COMPLETED 확인
        Race updatedRace = raceRepository.findById(race.getId()).get();
        assertThat(updatedRace.getStatus()).isEqualTo(RaceStatus.COMPLETED);
    }

    @Test
    @DisplayName("sessionKey 없는 레이스는 수집 없이 Job 완료")
    void collectRaceResult_noSessionKey_finishesWithoutData() throws Exception {
        // given
        Race race = raceRepository.save(Race.builder()
                .season(2025).round(2).raceName("Saudi GP")
                .circuit("Jeddah").country("Saudi Arabia")
                .raceDate(LocalDate.now()).status(RaceStatus.SCHEDULED)
                .sessionKey(null).build());

        // when
        JobParameters params = new JobParametersBuilder()
                .addLong("raceId", race.getId())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(raceResultCollectJob.collectRaceResultJob(), params);

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(raceResultRepository.findByRaceIdOrderByPositionAsc(race.getId())).isEmpty();
    }
}
