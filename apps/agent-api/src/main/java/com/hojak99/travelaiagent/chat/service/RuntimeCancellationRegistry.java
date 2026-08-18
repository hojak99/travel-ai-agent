package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.RuntimeCancellationSignal;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 실행 중인 Session의 취소 신호를 별도 보관해 State 잠금 밖에서도 취소할 수 있게 한다.
 */
@Component
public class RuntimeCancellationRegistry {
    private final Map<String, RuntimeCancellationSignal> activeRuns = new ConcurrentHashMap<>();

    /**
     * Session 실행 시작 시 다른 실행과 공유되지 않는 취소 신호를 등록한다.
     */
    public RuntimeCancellationSignal start(String sessionId) {
        RuntimeCancellationSignal signal = new RuntimeCancellationSignal();
        RuntimeCancellationSignal existing = activeRuns.putIfAbsent(sessionId, signal);
        if (existing != null) {
            throw new IllegalStateException("Session already has an active run: " + sessionId);
        }
        return signal;
    }

    /**
     * 활성 실행이 있을 때 한 번만 취소 상태로 전환한다.
     */
    public boolean cancel(String sessionId) {
        RuntimeCancellationSignal signal = activeRuns.get(sessionId);
        return signal != null && signal.cancel();
    }

    /**
     * 종료된 실행의 신호만 조건부 제거해 다음 실행의 신호를 지우지 않는다.
     */
    public void finish(String sessionId, RuntimeCancellationSignal signal) {
        activeRuns.remove(sessionId, signal);
    }
}
