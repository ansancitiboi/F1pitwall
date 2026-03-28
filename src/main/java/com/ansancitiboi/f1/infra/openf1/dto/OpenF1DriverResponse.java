package com.ansancitiboi.f1.infra.openf1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenF1DriverResponse(
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("name_acronym") String nameAcronym,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        @JsonProperty("team_name") String teamName,
        @JsonProperty("country_code") String countryCode,
        @JsonProperty("headshot_url") String headshotUrl
) {}
