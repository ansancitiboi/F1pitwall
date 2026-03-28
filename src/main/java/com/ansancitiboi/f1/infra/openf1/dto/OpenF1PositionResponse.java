package com.ansancitiboi.f1.infra.openf1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenF1PositionResponse(
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("position") Integer position,
        @JsonProperty("date") String date,
        @JsonProperty("session_key") Integer sessionKey
) {}
