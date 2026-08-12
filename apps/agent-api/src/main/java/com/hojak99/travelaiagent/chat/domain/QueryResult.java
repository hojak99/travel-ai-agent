package com.hojak99.travelaiagent.chat.domain;

public record QueryResult(
        String sessionId,
        String message,
        String status
) {
}
