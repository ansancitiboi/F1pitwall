package com.ansancitiboi.f1.infra.claude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClaudeRequest(
        String model,
        @JsonProperty("max_tokens") int maxTokens,
        List<Message> messages
) {
    public record Message(String role, String content) {}

    public static ClaudeRequest of(String model, int maxTokens, String userPrompt) {
        return new ClaudeRequest(model, maxTokens, List.of(new Message("user", userPrompt)));
    }
}
