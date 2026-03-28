package com.ansancitiboi.f1.domain.sse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RaceEventPayload {

    private RaceEventType type;
    private Integer driverNumber;
    private String driverCode;
    private String message;
    private String timestamp;
}
