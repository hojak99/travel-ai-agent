package com.hojak99.travelaiagent.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI 연결과 모델 실행 정책을 환경 설정에서 불변 값으로 읽는다.
 */
@ConfigurationProperties("openai")
public record OpenAiProperties(
        String apiKey,
        String model,
        String baseUrl,
        int maxOutputTokens,
        String reasoningEffort
) {
}
