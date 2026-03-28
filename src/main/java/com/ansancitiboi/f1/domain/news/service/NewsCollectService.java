package com.ansancitiboi.f1.domain.news.service;

import com.ansancitiboi.f1.domain.news.document.NewsDocument;
import com.ansancitiboi.f1.domain.news.entity.News;
import com.ansancitiboi.f1.domain.news.repository.NewsRepository;
import com.ansancitiboi.f1.domain.news.repository.NewsSearchRepository;
import com.ansancitiboi.f1.infra.claude.ClaudeClient;
import com.ansancitiboi.f1.infra.rss.RssCollector;
import com.ansancitiboi.f1.infra.rss.RssItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsCollectService {

    // 2025 시즌 드라이버 코드
    private static final List<String> DRIVER_CODES = List.of(
            "VER", "NOR", "LEC", "PIA", "SAI", "RUS", "HAM",
            "ANT", "ALO", "STR", "GAS", "HAD", "ALB", "SAR",
            "TSU", "LAW", "HUL", "BEA", "BOT", "BOR"
    );

    private static final List<String> TEAM_KEYWORDS = List.of(
            "Red Bull", "Ferrari", "McLaren", "Mercedes", "Aston Martin",
            "Alpine", "Williams", "RB", "Haas", "Sauber"
    );

    private final RssCollector rssCollector;
    private final ClaudeClient claudeClient;
    private final NewsRepository newsRepository;
    private final NewsSearchRepository newsSearchRepository;

    // 3시간마다 뉴스 수집
    @Scheduled(fixedDelay = 10800000)
    @Transactional
    public void collectNews() {
        log.info("뉴스 수집 시작");
        List<RssItem> items = rssCollector.collectAll();

        int saved = 0;
        for (RssItem item : items) {
            if (newsRepository.existsByUrl(item.url())) {
                continue;
            }

            try {
                String contentForSummary = item.title() + "\n\n" + item.description();
                String summary = claudeClient.summarize(contentForSummary);
                String tags = extractTags(contentForSummary);

                News news = News.builder()
                        .title(item.title())
                        .url(item.url())
                        .source(item.source())
                        .summary(summary)
                        .tags(tags)
                        .publishedAt(item.publishedAt())
                        .build();

                News savedNews = newsRepository.save(news);
                newsSearchRepository.save(NewsDocument.from(savedNews));
                saved++;

                // Claude API rate limit 방지
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("뉴스 처리 실패 - url: {}", item.url(), e);
            }
        }

        log.info("뉴스 수집 완료 - 신규 저장: {}건", saved);
    }

    private String extractTags(String content) {
        String upper = content.toUpperCase();

        List<String> foundDrivers = DRIVER_CODES.stream()
                .filter(code -> upper.contains(code))
                .collect(Collectors.toList());

        List<String> foundTeams = TEAM_KEYWORDS.stream()
                .filter(team -> upper.contains(team.toUpperCase()))
                .collect(Collectors.toList());

        foundDrivers.addAll(foundTeams);
        return String.join(",", foundDrivers);
    }
}
