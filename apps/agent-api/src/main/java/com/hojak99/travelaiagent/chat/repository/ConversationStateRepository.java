package com.hojak99.travelaiagent.chat.repository;

import com.hojak99.travelaiagent.chat.domain.ConversationState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationStateRepository {
    private final Map<String, ConversationState> conversationStateMap = new ConcurrentHashMap<>();

    public ConversationState loadOrCreate(String sessionId) {
        return conversationStateMap.computeIfAbsent(sessionId, ConversationState::create);
    }
}
