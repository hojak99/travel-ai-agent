package com.hojak99.travelaiagent.chat.domain;

/**
 * 외부 Chat 요청을 Runtime에 전달하기 위한 내부 입력 계약이다.
 */
public record QueryCommand(
        String sessionId,
        String message
) {
}
