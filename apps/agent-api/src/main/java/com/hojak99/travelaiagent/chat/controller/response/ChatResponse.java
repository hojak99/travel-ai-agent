package com.hojak99.travelaiagent.chat.controller.response;

/**
 * 한 번의 채팅 요청이 종료될 때 클라이언트에 반환할 외부 API 계약.
 */
public record ChatResponse(
        String sessionId,
        String message,
        String status
) {
    // TODO(Phase 2): 문자열 status를 구조화된 AgentDecision 응답으로 교체한다.
}
