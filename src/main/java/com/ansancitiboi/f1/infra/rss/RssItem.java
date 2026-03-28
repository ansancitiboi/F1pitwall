package com.ansancitiboi.f1.infra.rss;

import java.time.LocalDateTime;

public record RssItem(
        String title,
        String url,
        String description,
        String source,
        LocalDateTime publishedAt
) {}
