package com.hojak99.travelaiagent.chat.domain;

/**
 * QueryEngine이 Controller에 반환하는 세션 단위 실행 결과다.
 */
public record QueryResult(
        String sessionId,
        String message,
        String status
) {
}
