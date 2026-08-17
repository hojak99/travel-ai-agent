package com.hojak99.travelaiagent.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("openai")
public record OpenAiProperties(
        String apiKey,
        String model,
        String baseUrl,
        int maxOutputTokens,
        String reasoningEffort
) {
}
