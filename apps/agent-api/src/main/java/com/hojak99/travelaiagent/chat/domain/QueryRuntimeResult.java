package com.hojak99.travelaiagent.chat.domain;

public record QueryRuntimeResult(
        String message,
        RuntimeStatus status,
        int iterations
) {
}
