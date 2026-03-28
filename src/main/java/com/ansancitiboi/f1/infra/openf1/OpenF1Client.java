package com.ansancitiboi.f1.infra.openf1;

import com.ansancitiboi.f1.infra.openf1.dto.OpenF1DriverResponse;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1PitResponse;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1PositionResponse;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1RaceControlResponse;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1SessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenF1Client {

    private final WebClient webClient;

    @Value("${openf1.base-url}")
    private String baseUrl;

    // 시즌의 드라이버 목록 조회
    public List<OpenF1DriverResponse> getDrivers(int year) {
        return webClient.get()
                .uri(baseUrl + "/drivers?session_key=latest&meeting_key=latest")
                .retrieve()
                .bodyToFlux(OpenF1DriverResponse.class)
                .collectList()
                .block();
    }

    // 특정 세션의 현재 순위 조회 (실시간 폴링용)
    public List<OpenF1PositionResponse> getPositions(int sessionKey) {
        return webClient.get()
                .uri(baseUrl + "/position?session_key=" + sessionKey + "&date>=" + getLatestDate())
                .retrieve()
                .bodyToFlux(OpenF1PositionResponse.class)
                .collectList()
                .block();
    }

    // 현재 진행 중인 세션 조회
    public List<OpenF1SessionResponse> getSessions(int year) {
        return webClient.get()
                .uri(baseUrl + "/sessions?year=" + year + "&session_type=Race")
                .retrieve()
                .bodyToFlux(OpenF1SessionResponse.class)
                .collectList()
                .block();
    }

    // 세션의 최종 순위 조회 (레이스 종료 후)
    public List<OpenF1PositionResponse> getFinalPositions(int sessionKey) {
        return webClient.get()
                .uri(baseUrl + "/position?session_key=" + sessionKey)
                .retrieve()
                .bodyToFlux(OpenF1PositionResponse.class)
                .collectList()
                .block();
    }

    // 세션의 피트스톱 기록 조회
    public List<OpenF1PitResponse> getPitStops(int sessionKey) {
        return webClient.get()
                .uri(baseUrl + "/pit?session_key=" + sessionKey)
                .retrieve()
                .bodyToFlux(OpenF1PitResponse.class)
                .collectList()
                .block();
    }

    // 레이스 컨트롤 메시지 조회 (세이프티카, VSC 감지용)
    public List<OpenF1RaceControlResponse> getRaceControlMessages(int sessionKey, String dateFrom) {
        String uri = baseUrl + "/race_control?session_key=" + sessionKey;
        if (dateFrom != null) {
            uri += "&date%3E=" + dateFrom;
        }
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(OpenF1RaceControlResponse.class)
                .collectList()
                .block();
    }

    // 최근 5초 이내 데이터만 조회하기 위한 타임스탬프
    private String getLatestDate() {
        return java.time.Instant.now().minusSeconds(5).toString();
    }
}
