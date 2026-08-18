package com.hojak99.travelaiagent.llm;

import com.hojak99.travelaiagent.chat.domain.ConversationMessage;
import com.hojak99.travelaiagent.config.properties.OpenAiProperties;
import com.hojak99.travelaiagent.llm.domain.OpenAiOutputContent;
import com.hojak99.travelaiagent.llm.domain.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * OpenAI Responses API의 요청·응답 형식을 내부 LlmClient 계약으로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class OpenAiResponsesClient implements LlmClient {

    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;

    /**
     * 역할별 대화를 API 입력으로 변환하고 첫 output_text를 Runtime 응답으로 반환한다.
     */
    @Override
    public String generate(String instructions, List<ConversationMessage> messages) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY 환경변수가 설정되지 않았습니다.");
        }

        List<Map<String, String>> input = messages.stream()
                .map(message -> Map.of(
                        "role", message.role().name().toLowerCase(Locale.ROOT),
                        "content", message.content()))
                .toList();

        OpenAiResponse response = openAiRestClient.post()
                .uri("/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .body(Map.of(
                        "model", properties.model(),
                        "instructions", instructions,
                        "input", input,
                        "max_output_tokens", properties.maxOutputTokens(),
                        "reasoning", Map.of("effort", properties.reasoningEffort())))
                .retrieve()
                .body(OpenAiResponse.class);

        if (response == null) {
            throw new IllegalStateException("OpenAI API가 빈 응답을 반환했습니다.");
        }

        return response.output().stream()
                .filter(item -> "message".equals(item.type()))
                .flatMap(item -> item.content().stream())
                .filter(content -> "output_text".equals(content.type()))
                .map(OpenAiOutputContent::text)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("OpenAI 응답에서 output_text를 찾을 수 없습니다."));
    }
}
