package com.ansancitiboi.f1.domain.news.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String url;

    private String source;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;

    // 쉼표 구분 드라이버 코드 (예: "VER,HAM,LEC")
    private String tags;

    private LocalDateTime publishedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public News(String title, String url, String source, String summary,
                String tags, LocalDateTime publishedAt) {
        this.title = title;
        this.url = url;
        this.source = source;
        this.summary = summary;
        this.tags = tags;
        this.publishedAt = publishedAt;
    }
}
