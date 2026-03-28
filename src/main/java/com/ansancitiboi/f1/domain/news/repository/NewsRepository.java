package com.ansancitiboi.f1.domain.news.repository;

import com.ansancitiboi.f1.domain.news.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    boolean existsByUrl(String url);

    Page<News> findAllByOrderByPublishedAtDesc(Pageable pageable);

    List<News> findByTagsContainingOrderByPublishedAtDesc(String tag);
}
