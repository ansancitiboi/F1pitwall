package com.ansancitiboi.f1.domain.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterManager {

    private static final long TIMEOUT = 30 * 60 * 1000L; // 30분

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE 연결 종료 - userId: {}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SSE 타임아웃 - userId: {}", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            log.warn("SSE 에러 - userId: {}, error: {}", userId, e.getMessage());
        });

        emitters.put(userId, emitter);

        // 연결 확인용 초기 이벤트
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        log.info("SSE 연결 - userId: {}, 현재 연결 수: {}", userId, emitters.size());
        return emitter;
    }

    public void sendToUser(Long userId, RaceEventPayload payload) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name(payload.getType().name().toLowerCase())
                    .data(payload));
        } catch (IOException e) {
            emitters.remove(userId);
            log.warn("SSE 전송 실패 - userId: {}", userId);
        }
    }

    public void sendToAll(RaceEventPayload payload) {
        Set<Long> userIds = emitters.keySet();
        for (Long userId : userIds) {
            sendToUser(userId, payload);
        }
    }

    public boolean isConnected(Long userId) {
        return emitters.containsKey(userId);
    }

    public int getConnectedCount() {
        return emitters.size();
    }
}
