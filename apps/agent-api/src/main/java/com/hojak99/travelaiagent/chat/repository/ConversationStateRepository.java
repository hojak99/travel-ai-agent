package com.hojak99.travelaiagent.chat.repository;

import com.hojak99.travelaiagent.chat.domain.ConversationState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session별 ConversationState의 단일 인스턴스를 보관하는 Phase 1 메모리 저장소다.
 */
@Component
public class ConversationStateRepository {
    private final Map<String, ConversationState> conversationStateMap = new ConcurrentHashMap<>();

    /**
     * 첫 메시지에서는 State를 만들고 후속 메시지에서는 같은 인스턴스를 반환한다.
     */
    public ConversationState loadOrCreate(String sessionId) {
        return conversationStateMap.computeIfAbsent(sessionId, ConversationState::create);
    }

    /**
     * 조회 요청이 존재하지 않는 Session을 생성하지 않도록 Optional로 반환한다.
     */
    public Optional<ConversationState> find(String sessionId) {
        return Optional.ofNullable(conversationStateMap.get(sessionId));
    }
}
