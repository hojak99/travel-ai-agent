package com.hojak99.travelaiagent.chat.domain;

public record QueryCommand(
        String sessionId,
        String message
) {
}
