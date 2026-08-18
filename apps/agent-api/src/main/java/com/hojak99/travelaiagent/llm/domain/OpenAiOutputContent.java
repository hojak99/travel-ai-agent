package com.hojak99.travelaiagent.llm.domain;

/**
 * Responses API content에서 output_text를 식별하고 본문을 읽기 위한 계약이다.
 */
public record OpenAiOutputContent(String type, String text) {
}
