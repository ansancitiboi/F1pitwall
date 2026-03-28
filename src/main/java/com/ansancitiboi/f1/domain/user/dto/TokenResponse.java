package com.ansancitiboi.f1.domain.user.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
