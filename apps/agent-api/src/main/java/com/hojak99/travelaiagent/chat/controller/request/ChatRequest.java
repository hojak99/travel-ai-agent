package com.hojak99.travelaiagent.chat.controller.request;

/**
 * 사용자가 한 번의 채팅 요청에서 보내는 외부 API 계약.
 */
public record ChatRequest(
        String sessionId,
        String message
) {
    // TODO: sessionId와 message의 입력 검증 규칙을 정한다.
}
