package com.hojak99.travelaiagent.chat.controller.response;

/**
 * 취소 신호가 실행 중인 세션에 실제로 전달됐는지 나타낸다.
 */
public record CancelChatResponse(
        String sessionId,
        boolean cancelled
) {
}
