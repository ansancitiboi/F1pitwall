package com.ansancitiboi.f1.domain.driver.service;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import com.ansancitiboi.f1.domain.driver.repository.DriverRepository;
import com.ansancitiboi.f1.infra.openf1.OpenF1Client;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1DriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverSyncService {

    private final DriverRepository driverRepository;
    private final OpenF1Client openF1Client;

    // 매일 오전 6시 — 최신 드라이버 라인업 동기화
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void syncDrivers() {
        int currentYear = LocalDate.now().getYear();
        log.info("드라이버 동기화 시작 - season: {}", currentYear);

        List<OpenF1DriverResponse> responses = openF1Client.getDrivers(currentYear);
        if (responses == null || responses.isEmpty()) {
            log.warn("드라이버 데이터 없음 - season: {}", currentYear);
            return;
        }

        int created = 0, updated = 0;

        for (OpenF1DriverResponse res : responses) {
            if (res.driverNumber() == null) continue;

            String code = res.nameAcronym() != null ? res.nameAcronym() : "N/A";
            String firstName = res.firstName() != null ? res.firstName() : "";
            String lastName = res.lastName() != null ? res.lastName() : "";
            String teamName = res.teamName() != null ? res.teamName() : "";
            String nationality = res.countryCode() != null ? res.countryCode() : "";
            String headshotUrl = res.headshotUrl() != null ? res.headshotUrl() : "";

            var existing = driverRepository.findByDriverNumber(res.driverNumber());

            if (existing.isPresent()) {
                existing.get().update(code, firstName, lastName, teamName, nationality, headshotUrl);
                updated++;
            } else {
                driverRepository.save(Driver.builder()
                        .driverNumber(res.driverNumber())
                        .code(code)
                        .firstName(firstName)
                        .lastName(lastName)
                        .teamName(teamName)
                        .nationality(nationality)
                        .headshotUrl(headshotUrl)
                        .build());
                created++;
                log.info("신규 드라이버 등록 - #{} {}", res.driverNumber(), lastName);
            }
        }

        log.info("드라이버 동기화 완료 - 신규: {}, 업데이트: {}", created, updated);
    }
}
