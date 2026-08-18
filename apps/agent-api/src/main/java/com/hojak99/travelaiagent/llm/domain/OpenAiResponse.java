package com.hojak99.travelaiagent.llm.domain;

import java.util.List;

/**
 * Responses API 최상위 output 배열을 역직렬화하는 외부 응답 계약이다.
 */
public record OpenAiResponse(List<OpenAiOutputItem> output) {
}
