package com.ansancitiboi.f1.domain.race.polling;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import com.ansancitiboi.f1.domain.driver.repository.DriverRepository;
import com.ansancitiboi.f1.domain.driver.repository.UserDriverSubscriptionRepository;
import com.ansancitiboi.f1.domain.race.entity.Race;
import com.ansancitiboi.f1.domain.race.entity.RaceStatus;
import com.ansancitiboi.f1.domain.race.repository.RaceRepository;
import com.ansancitiboi.f1.domain.sse.RaceEventPayload;
import com.ansancitiboi.f1.domain.sse.RaceEventType;
import com.ansancitiboi.f1.domain.sse.SseEmitterManager;
import com.ansancitiboi.f1.infra.openf1.OpenF1Client;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1PitResponse;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1PositionResponse;
import com.ansancitiboi.f1.infra.openf1.dto.OpenF1RaceControlResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RaceEventPollingServiceTest {

    @InjectMocks RaceEventPollingService pollingService;

    @Mock OpenF1Client openF1Client;
    @Mock RaceRepository raceRepository;
    @Mock DriverRepository driverRepository;
    @Mock UserDriverSubscriptionRepository subscriptionRepository;
    @Mock SseEmitterManager sseEmitterManager;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @Mock ObjectMapper objectMapper;

    Race activeRace;
    Driver driver;

    @BeforeEach
    void setUp() throws Exception {
        activeRace = Race.builder()
                .season(2025).round(1).raceName("Bahrain GP")
                .circuit("Bahrain").country("Bahrain")
                .status(RaceStatus.IN_PROGRESS).sessionKey(9999).build();

        driver = Driver.builder()
                .driverNumber(1).code("VER").firstName("Max").lastName("Verstappen")
                .teamName("Red Bull Racing").nationality("Dutch").headshotUrl("").build();

        lenient().when(sseEmitterManager.getConnectedCount()).thenReturn(1);
        lenient().when(raceRepository.findByStatus(RaceStatus.IN_PROGRESS)).thenReturn(Optional.of(activeRace));
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(valueOps.get(anyString())).thenReturn(null);
        lenient().when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(new java.util.HashMap<>());
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    }

    @Test
    @DisplayName("SSE 연결 유저 없으면 폴링 스킵")
    void pollRaceEvents_noConnectedUsers_skips() {
        when(sseEmitterManager.getConnectedCount()).thenReturn(0);

        pollingService.pollRaceEvents();

        verify(raceRepository, never()).findByStatus(any());
    }

    @Test
    @DisplayName("진행 중인 레이스 없으면 폴링 스킵")
    void pollRaceEvents_noActiveRace_skips() {
        when(raceRepository.findByStatus(RaceStatus.IN_PROGRESS)).thenReturn(Optional.empty());

        pollingService.pollRaceEvents();

        verify(openF1Client, never()).getPositions(anyInt());
    }

    @Test
    @DisplayName("세이프티카 이벤트 감지 시 전체 유저에게 SSE 전송")
    void detectSafetyCar_sendsToAllUsers() throws Exception {
        when(openF1Client.getPositions(9999)).thenReturn(List.of());
        when(openF1Client.getPitStops(9999)).thenReturn(List.of());
        when(openF1Client.getRaceControlMessages(eq(9999), any()))
                .thenReturn(List.of(
                        new OpenF1RaceControlResponse(9999, "2025-03-02T15:00:00",
                                10, null, "SAFETY_CAR", "SAFETY CAR DEPLOYED", "Flag", "Track")
                ));

        pollingService.pollRaceEvents();

        ArgumentCaptor<RaceEventPayload> captor = ArgumentCaptor.forClass(RaceEventPayload.class);
        verify(sseEmitterManager).sendToAll(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(RaceEventType.SAFETY_CAR);
    }

    @Test
    @DisplayName("VSC 이벤트 감지 시 전체 유저에게 SSE 전송")
    void detectVsc_sendsToAllUsers() throws Exception {
        when(openF1Client.getPositions(9999)).thenReturn(List.of());
        when(openF1Client.getPitStops(9999)).thenReturn(List.of());
        when(openF1Client.getRaceControlMessages(eq(9999), any()))
                .thenReturn(List.of(
                        new OpenF1RaceControlResponse(9999, "2025-03-02T15:10:00",
                                15, null, "VIRTUAL_SAFETY_CAR", "VIRTUAL SAFETY CAR", "Flag", "Track")
                ));

        pollingService.pollRaceEvents();

        ArgumentCaptor<RaceEventPayload> captor = ArgumentCaptor.forClass(RaceEventPayload.class);
        verify(sseEmitterManager).sendToAll(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(RaceEventType.VIRTUAL_SAFETY_CAR);
    }

    @Test
    @DisplayName("피트스톱 감지 시 구독 유저에게 SSE 전송")
    void detectPitStop_notifiesSubscribers() throws Exception {
        when(openF1Client.getPositions(9999)).thenReturn(List.of());
        when(openF1Client.getRaceControlMessages(eq(9999), any())).thenReturn(List.of());
        when(openF1Client.getPitStops(9999)).thenReturn(List.of(
                new OpenF1PitResponse(1, 20, 2.345, 9999)
        ));
        when(driverRepository.findByDriverNumber(1)).thenReturn(Optional.of(driver));
        when(subscriptionRepository.findUserIdsByDriverId(any())).thenReturn(List.of(100L));
        when(sseEmitterManager.isConnected(100L)).thenReturn(true);

        pollingService.pollRaceEvents();

        ArgumentCaptor<RaceEventPayload> captor = ArgumentCaptor.forClass(RaceEventPayload.class);
        verify(sseEmitterManager).sendToUser(eq(100L), captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(RaceEventType.PIT_STOP);
        assertThat(captor.getValue().getDriverCode()).isEqualTo("VER");
    }
}
