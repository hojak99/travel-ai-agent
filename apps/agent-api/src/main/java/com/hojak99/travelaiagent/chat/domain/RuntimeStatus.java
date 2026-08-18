package com.hojak99.travelaiagent.chat.domain;

/**
 * 사용자 대기와 실패를 구분하는 QueryRuntime의 명시적 종료 상태다.
 */
public enum RuntimeStatus {
    NEED_USER_INPUT,
    FINAL,
    MAX_ITERATIONS,
    ERROR
}
