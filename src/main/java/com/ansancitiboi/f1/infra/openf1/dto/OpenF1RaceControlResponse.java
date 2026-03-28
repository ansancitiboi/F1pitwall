package com.ansancitiboi.f1.infra.openf1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenF1RaceControlResponse(
        @JsonProperty("session_key") Integer sessionKey,
        @JsonProperty("date") String date,
        @JsonProperty("lap_number") Integer lapNumber,
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("flag") String flag,
        @JsonProperty("message") String message,
        @JsonProperty("category") String category,
        @JsonProperty("scope") String scope
) {}
