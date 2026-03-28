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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaceEventPollingService {

    private static final String KEY_POSITIONS = "f1:positions:";
    private static final String KEY_PITSTOP_COUNT = "f1:pitstop_count:";
    private static final String KEY_RACE_CONTROL_TS = "f1:racecontrol_ts:";

    private final OpenF1Client openF1Client;
    private final RaceRepository raceRepository;
    private final DriverRepository driverRepository;
    private final UserDriverSubscriptionRepository subscriptionRepository;
    private final SseEmitterManager sseEmitterManager;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 3000)
    public void pollRaceEvents() {
        if (sseEmitterManager.getConnectedCount() == 0) {
            return;
        }

        Optional<Race> activeRace = raceRepository.findByStatus(RaceStatus.IN_PROGRESS);
        if (activeRace.isEmpty() || activeRace.get().getSessionKey() == null) {
            return;
        }

        Race race = activeRace.get();
        int sessionKey = race.getSessionKey();

        try {
            detectPositionChanges(sessionKey);
            detectPitStops(sessionKey);
            detectRaceControlEvents(sessionKey);
        } catch (Exception e) {
            log.error("폴링 중 오류 발생 - sessionKey: {}", sessionKey, e);
        }
    }

    private void detectPositionChanges(int sessionKey) {
        List<OpenF1PositionResponse> positions = openF1Client.getPositions(sessionKey);
        if (positions == null || positions.isEmpty()) return;

        // 드라이버번호별 최신 포지션만 유지
        Map<Integer, Integer> current = new HashMap<>();
        for (OpenF1PositionResponse p : positions) {
            if (p.driverNumber() != null && p.position() != null) {
                current.put(p.driverNumber(), p.position());
            }
        }

        String redisKey = KEY_POSITIONS + sessionKey;
        Map<Integer, Integer> previous = getFromRedis(redisKey, new TypeReference<>() {});

        for (Map.Entry<Integer, Integer> entry : current.entrySet()) {
            Integer driverNumber = entry.getKey();
            Integer newPosition = entry.getValue();
            Integer oldPosition = previous.get(driverNumber);

            if (oldPosition != null && !oldPosition.equals(newPosition)) {
                String driverCode = getDriverCode(driverNumber);
                String message = String.format("%s: %d위 → %d위", driverCode, oldPosition, newPosition);

                RaceEventPayload payload = RaceEventPayload.builder()
                        .type(RaceEventType.POSITION_CHANGE)
                        .driverNumber(driverNumber)
                        .driverCode(driverCode)
                        .message(message)
                        .timestamp(Instant.now().toString())
                        .build();

                notifySubscribers(driverNumber, payload);
            }
        }

        // 상태 갱신 (현재 + 이전 merged)
        previous.putAll(current);
        saveToRedis(redisKey, previous);
    }

    private void detectPitStops(int sessionKey) {
        List<OpenF1PitResponse> pits = openF1Client.getPitStops(sessionKey);
        if (pits == null || pits.isEmpty()) return;

        // 드라이버번호별 피트스톱 횟수 집계
        Map<Integer, Integer> currentCounts = new HashMap<>();
        for (OpenF1PitResponse pit : pits) {
            if (pit.driverNumber() != null) {
                currentCounts.merge(pit.driverNumber(), 1, Integer::sum);
            }
        }

        String redisKey = KEY_PITSTOP_COUNT + sessionKey;
        Map<Integer, Integer> previousCounts = getFromRedis(redisKey, new TypeReference<>() {});

        for (Map.Entry<Integer, Integer> entry : currentCounts.entrySet()) {
            Integer driverNumber = entry.getKey();
            Integer newCount = entry.getValue();
            Integer oldCount = previousCounts.getOrDefault(driverNumber, 0);

            if (newCount > oldCount) {
                String driverCode = getDriverCode(driverNumber);
                String message = String.format("%s 피트인! (%d번째 피트스톱)", driverCode, newCount);

                RaceEventPayload payload = RaceEventPayload.builder()
                        .type(RaceEventType.PIT_STOP)
                        .driverNumber(driverNumber)
                        .driverCode(driverCode)
                        .message(message)
                        .timestamp(Instant.now().toString())
                        .build();

                notifySubscribers(driverNumber, payload);
            }
        }

        saveToRedis(redisKey, currentCounts);
    }

    private void detectRaceControlEvents(int sessionKey) {
        String tsKey = KEY_RACE_CONTROL_TS + sessionKey;
        String lastDate = redisTemplate.opsForValue().get(tsKey);

        List<OpenF1RaceControlResponse> messages = openF1Client.getRaceControlMessages(sessionKey, lastDate);
        if (messages == null || messages.isEmpty()) return;

        String latestDate = null;

        for (OpenF1RaceControlResponse msg : messages) {
            if (msg.flag() == null) continue;

            RaceEventType type = switch (msg.flag()) {
                case "SAFETY_CAR" -> RaceEventType.SAFETY_CAR;
                case "VIRTUAL_SAFETY_CAR" -> RaceEventType.VIRTUAL_SAFETY_CAR;
                default -> null;
            };

            if (type != null) {
                String text = msg.message() != null ? msg.message() : msg.flag();
                RaceEventPayload payload = RaceEventPayload.builder()
                        .type(type)
                        .message(text)
                        .timestamp(msg.date() != null ? msg.date() : Instant.now().toString())
                        .build();

                sseEmitterManager.sendToAll(payload);
                log.info("레이스 컨트롤 이벤트 전송: {}", type);
            }

            if (msg.date() != null) {
                if (latestDate == null || msg.date().compareTo(latestDate) > 0) {
                    latestDate = msg.date();
                }
            }
        }

        if (latestDate != null) {
            redisTemplate.opsForValue().set(tsKey, latestDate);
        }
    }

    private void notifySubscribers(Integer driverNumber, RaceEventPayload payload) {
        Optional<Driver> driverOpt = driverRepository.findByDriverNumber(driverNumber);
        if (driverOpt.isEmpty()) return;

        List<Long> userIds = subscriptionRepository.findUserIdsByDriverId(driverOpt.get().getId());
        for (Long userId : userIds) {
            if (sseEmitterManager.isConnected(userId)) {
                sseEmitterManager.sendToUser(userId, payload);
            }
        }
    }

    private String getDriverCode(Integer driverNumber) {
        return driverRepository.findByDriverNumber(driverNumber)
                .map(Driver::getCode)
                .orElse("No." + driverNumber);
    }

    private <T> Map<Integer, T> getFromRedis(String key, TypeReference<Map<Integer, T>> typeRef) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return new HashMap<>();
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.warn("Redis 읽기 실패 - key: {}", key, e);
            return new HashMap<>();
        }
    }

    private void saveToRedis(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        } catch (Exception e) {
            log.warn("Redis 저장 실패 - key: {}", key, e);
        }
    }
}
