package com.hojak99.travelaiagent.chat.facade;

import com.hojak99.travelaiagent.chat.controller.request.ChatRequest;
import com.hojak99.travelaiagent.chat.domain.ConversationState;
import com.hojak99.travelaiagent.chat.service.ConversationStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryEngineFacade {

    private final ConversationStateService conversationStateService;

    public void query(ChatRequest request) {
        ConversationState conversationState = conversationStateService.get(request.sessionId());
    }
}
