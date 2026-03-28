package com.ansancitiboi.f1.domain.news.document;

import com.ansancitiboi.f1.domain.news.entity.News;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "f1_news")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String summary;

    @Field(type = FieldType.Keyword)
    private String url;

    @Field(type = FieldType.Keyword)
    private String source;

    @Field(type = FieldType.Keyword)
    private String tags;

    @Field(type = FieldType.Date)
    private LocalDateTime publishedAt;

    public static NewsDocument from(News news) {
        return NewsDocument.builder()
                .id(String.valueOf(news.getId()))
                .title(news.getTitle())
                .summary(news.getSummary())
                .url(news.getUrl())
                .source(news.getSource())
                .tags(news.getTags())
                .publishedAt(news.getPublishedAt())
                .build();
    }
}
