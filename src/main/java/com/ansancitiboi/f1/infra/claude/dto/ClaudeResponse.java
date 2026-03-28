package com.ansancitiboi.f1.infra.claude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClaudeResponse(
        String id,
        List<ContentBlock> content
) {
    public record ContentBlock(String type, String text) {}

    @JsonProperty("stop_reason")
    public String stopReason() { return null; }

    public String extractText() {
        if (content == null || content.isEmpty()) return "";
        return content.stream()
                .filter(b -> "text".equals(b.type()))
                .map(ContentBlock::text)
                .findFirst()
                .orElse("");
    }
}
