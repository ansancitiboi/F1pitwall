package com.ansancitiboi.f1.domain.news.dto;

import com.ansancitiboi.f1.domain.news.document.NewsDocument;

import java.time.LocalDateTime;

public record NewsSearchResponse(
        String id,
        String title,
        String url,
        String source,
        String summary,
        LocalDateTime publishedAt
) {
    public static NewsSearchResponse from(NewsDocument doc) {
        return new NewsSearchResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getUrl(),
                doc.getSource(),
                doc.getSummary(),
                doc.getPublishedAt()
        );
    }
}
