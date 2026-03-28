package com.ansancitiboi.f1.infra.rss;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class RssCollector {

    // F1 주요 RSS 피드
    private static final List<String[]> RSS_FEEDS = List.of(
            new String[]{"https://www.autosport.com/rss/f1/news/", "autosport"},
            new String[]{"https://feeds.bbci.co.uk/sport/formula1/rss.xml", "bbc"},
            new String[]{"https://motorsport.com/rss/f1/news/", "motorsport"}
    );

    public List<RssItem> collectAll() {
        List<RssItem> items = new ArrayList<>();
        for (String[] feed : RSS_FEEDS) {
            items.addAll(collect(feed[0], feed[1]));
        }
        return items;
    }

    private List<RssItem> collect(String feedUrl, String source) {
        List<RssItem> items = new ArrayList<>();
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(URI.create(feedUrl).toURL()));

            for (SyndEntry entry : feed.getEntries()) {
                String url = entry.getLink();
                String title = entry.getTitle();
                String description = extractDescription(entry);
                LocalDateTime publishedAt = toLocalDateTime(entry.getPublishedDate());

                if (url != null && title != null) {
                    items.add(new RssItem(title.trim(), url.trim(), description, source, publishedAt));
                }
            }
            log.info("RSS 수집 완료 - source: {}, count: {}", source, items.size());
        } catch (Exception e) {
            log.warn("RSS 수집 실패 - source: {}, error: {}", source, e.getMessage());
        }
        return items;
    }

    private String extractDescription(SyndEntry entry) {
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            // HTML 태그 제거
            return entry.getDescription().getValue().replaceAll("<[^>]*>", "").trim();
        }
        return "";
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return LocalDateTime.now();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
