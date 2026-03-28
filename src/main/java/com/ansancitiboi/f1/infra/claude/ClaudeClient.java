package com.ansancitiboi.f1.infra.claude;

import com.ansancitiboi.f1.infra.claude.dto.ClaudeRequest;
import com.ansancitiboi.f1.infra.claude.dto.ClaudeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ClaudeClient {

    private final WebClient webClient;
    private final String model;
    private final int maxTokens;

    public ClaudeClient(
            WebClient webClient,
            @Value("${claude.api-key}") String apiKey,
            @Value("${claude.base-url}") String baseUrl,
            @Value("${claude.model}") String model,
            @Value("${claude.max-tokens}") int maxTokens
    ) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }

    public String summarize(String newsContent) {
        String prompt = """
                다음 F1 뉴스를 한국어로 3문장 이내로 간결하게 요약해줘.
                핵심 내용(드라이버, 팀, 결과, 사건)을 포함해야 해.
                요약문만 반환하고 다른 설명은 필요 없어.

                뉴스 내용:
                %s
                """.formatted(newsContent);

        try {
            ClaudeResponse response = webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(ClaudeRequest.of(model, maxTokens, prompt))
                    .retrieve()
                    .bodyToMono(ClaudeResponse.class)
                    .block();

            return response != null ? response.extractText() : "";
        } catch (Exception e) {
            log.error("Claude API 호출 실패", e);
            return "";
        }
    }
}
