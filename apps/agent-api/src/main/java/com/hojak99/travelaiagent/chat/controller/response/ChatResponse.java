package com.hojak99.travelaiagent.chat.controller.response;

/**
 * 한 번의 채팅 요청이 종료될 때 클라이언트에 반환할 외부 API 계약.
 */
public record ChatResponse(
        String sessionId,
        String message
) {
    // TODO: ASK_USER, FINAL, ERROR 등 종료 상태와 구조화 결과를 설계한다.
}
