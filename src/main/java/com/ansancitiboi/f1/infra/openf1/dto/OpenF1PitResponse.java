package com.ansancitiboi.f1.infra.openf1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenF1PitResponse(
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("lap_number") Integer lapNumber,
        @JsonProperty("pit_duration") Double pitDuration,
        @JsonProperty("session_key") Integer sessionKey
) {}
