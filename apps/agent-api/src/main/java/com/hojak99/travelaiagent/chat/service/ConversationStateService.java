package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.ConversationState;
import com.hojak99.travelaiagent.chat.repository.ConversationStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationStateService {

    private final ConversationStateRepository conversationStateRepository;

    public ConversationState get(String sessionId) {
        return conversationStateRepository.selectBy(sessionId).orElseThrow();
    }
}
