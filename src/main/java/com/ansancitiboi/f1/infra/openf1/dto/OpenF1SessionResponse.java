package com.ansancitiboi.f1.infra.openf1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenF1SessionResponse(
        @JsonProperty("session_key") Integer sessionKey,
        @JsonProperty("session_name") String sessionName,
        @JsonProperty("session_type") String sessionType,
        @JsonProperty("meeting_name") String meetingName,
        @JsonProperty("country_name") String countryName,
        @JsonProperty("circuit_short_name") String circuitShortName,
        @JsonProperty("year") Integer year,
        @JsonProperty("date_start") String dateStart,
        @JsonProperty("date_end") String dateEnd
) {}
