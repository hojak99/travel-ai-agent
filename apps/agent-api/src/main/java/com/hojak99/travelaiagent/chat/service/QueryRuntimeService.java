package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.ConversationState;
import com.hojak99.travelaiagent.chat.domain.QueryRuntimeResult;
import org.springframework.stereotype.Service;

@Service
public class QueryRuntimeService {
    public QueryRuntimeResult run(ConversationState conversationState, String message) {
        conversationState.addUserMessage(message);
        String messageResult = "message result";
        conversationState.addAssistantMessage(messageResult);
        return new QueryRuntimeResult(messageResult, "COMPLETED");
    }
}
