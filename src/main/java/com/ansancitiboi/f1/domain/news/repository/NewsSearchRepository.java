package com.ansancitiboi.f1.domain.news.repository;

import com.ansancitiboi.f1.domain.news.document.NewsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface NewsSearchRepository extends ElasticsearchRepository<NewsDocument, String> {

    List<NewsDocument> findByTitleContainingOrSummaryContaining(String title, String summary);
}
