package com.ansancitiboi.f1.domain.news.controller;

import com.ansancitiboi.f1.domain.news.dto.NewsResponse;
import com.ansancitiboi.f1.domain.news.dto.NewsSearchResponse;
import com.ansancitiboi.f1.domain.news.service.NewsService;
import com.ansancitiboi.f1.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ApiResponse<List<NewsResponse>> getLatestNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(newsService.getLatestNews(page, size));
    }

    @GetMapping("/{newsId}")
    public ApiResponse<NewsResponse> getNews(@PathVariable Long newsId) {
        return ApiResponse.ok(newsService.getNewsById(newsId));
    }

    @GetMapping("/drivers/{driverCode}")
    public ApiResponse<List<NewsResponse>> getNewsByDriver(@PathVariable String driverCode) {
        return ApiResponse.ok(newsService.getNewsByDriverCode(driverCode));
    }

    @GetMapping("/search")
    public ApiResponse<List<NewsSearchResponse>> search(@RequestParam String q) {
        return ApiResponse.ok(newsService.search(q));
    }
}
