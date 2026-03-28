package com.ansancitiboi.f1.domain.news.dto;

import com.ansancitiboi.f1.domain.news.entity.News;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record NewsResponse(
        Long id,
        String title,
        String url,
        String source,
        String summary,
        List<String> tags,
        LocalDateTime publishedAt
) {
    public static NewsResponse from(News news) {
        List<String> tagList = (news.getTags() != null && !news.getTags().isBlank())
                ? Arrays.asList(news.getTags().split(","))
                : List.of();

        return new NewsResponse(
                news.getId(),
                news.getTitle(),
                news.getUrl(),
                news.getSource(),
                news.getSummary(),
                tagList,
                news.getPublishedAt()
        );
    }
}
