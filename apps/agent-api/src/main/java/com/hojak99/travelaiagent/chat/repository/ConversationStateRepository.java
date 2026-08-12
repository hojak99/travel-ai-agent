package com.hojak99.travelaiagent.chat.repository;

import com.hojak99.travelaiagent.chat.domain.ConversationState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ConversationStateRepository {

    private final Map<String, ConversationState> conversationStateMap = new HashMap<>();

    public Optional<ConversationState> selectBy(String sessionId) {
        return Optional.ofNullable(conversationStateMap.get(sessionId));
    }

    public void insert(ConversationState conversationState) {
        conversationStateMap.put(conversationState.getSessionId(), conversationState);
    }
}
