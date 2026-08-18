package com.hojak99.travelaiagent.chat.domain;

/**
 * 한 번의 Runtime 실행이 끝난 이유와 사용한 반복 횟수를 전달한다.
 */
public record QueryRuntimeResult(
        String message,
        RuntimeStatus status,
        int iterations
) {
}
