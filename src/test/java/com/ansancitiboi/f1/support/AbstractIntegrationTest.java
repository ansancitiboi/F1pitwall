package com.ansancitiboi.f1.support;

import com.ansancitiboi.f1.domain.news.repository.NewsSearchRepository;
import com.ansancitiboi.f1.infra.claude.ClaudeClient;
import com.ansancitiboi.f1.infra.rss.RssCollector;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("f1test")
            .withUsername("test")
            .withPassword("test");

    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    static {
        MYSQL.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        // ES는 실제 연결 없이 repository만 mock 처리
        registry.add("spring.elasticsearch.uris", () -> "http://localhost:19200");
    }

    // ES / Claude / RSS 관련 Bean은 mock으로 교체 (외부 연결 방지)
    @MockitoBean
    protected NewsSearchRepository newsSearchRepository;

    @MockitoBean
    protected ClaudeClient claudeClient;

    @MockitoBean
    protected RssCollector rssCollector;
}
