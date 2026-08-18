package com.hojak99.travelaiagent.chat.domain;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 여러 Thread가 안전하게 공유하는 한 번의 Runtime 실행용 협력적 취소 상태다.
 */
public final class RuntimeCancellationSignal {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * 활성 실행을 최초 한 번만 취소 상태로 전환한다.
     */
    public boolean cancel() {
        return cancelled.compareAndSet(false, true);
    }
}
