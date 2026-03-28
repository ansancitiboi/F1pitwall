package com.ansancitiboi.f1.domain.news.service;

import com.ansancitiboi.f1.domain.news.document.NewsDocument;
import com.ansancitiboi.f1.domain.news.dto.NewsResponse;
import com.ansancitiboi.f1.domain.news.dto.NewsSearchResponse;
import com.ansancitiboi.f1.domain.news.entity.News;
import com.ansancitiboi.f1.domain.news.repository.NewsRepository;
import com.ansancitiboi.f1.domain.news.repository.NewsSearchRepository;
import com.ansancitiboi.f1.global.common.exception.BusinessException;
import com.ansancitiboi.f1.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final NewsSearchRepository newsSearchRepository;

    public List<NewsResponse> getLatestNews(int page, int size) {
        return newsRepository.findAllByOrderByPublishedAtDesc(PageRequest.of(page, size))
                .map(NewsResponse::from)
                .toList();
    }

    public NewsResponse getNewsById(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEWS_NOT_FOUND));
        return NewsResponse.from(news);
    }

    public List<NewsResponse> getNewsByDriverCode(String driverCode) {
        return newsRepository.findByTagsContainingOrderByPublishedAtDesc(driverCode.toUpperCase()).stream()
                .map(NewsResponse::from)
                .toList();
    }

    public List<NewsSearchResponse> search(String query) {
        List<NewsDocument> results = newsSearchRepository
                .findByTitleContainingOrSummaryContaining(query, query);
        return results.stream()
                .map(NewsSearchResponse::from)
                .toList();
    }
}
