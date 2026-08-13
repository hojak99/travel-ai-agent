package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.ConversationState;
import com.hojak99.travelaiagent.chat.domain.QueryCommand;
import com.hojak99.travelaiagent.chat.domain.QueryResult;
import com.hojak99.travelaiagent.chat.domain.QueryRuntimeResult;
import com.hojak99.travelaiagent.chat.repository.ConversationStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryEngineService {

    private final ConversationStateRepository conversationStateRepository;
    private final QueryRuntimeService queryRuntimeService;

    public QueryResult submit(QueryCommand queryCommand) {
        ConversationState conversationState = conversationStateRepository.loadOrCreate(queryCommand.sessionId());
        synchronized (conversationState) {
            QueryRuntimeResult queryRuntimeResult = queryRuntimeService.run(conversationState, queryCommand.message());
            return new QueryResult(conversationState.getSessionId(), queryRuntimeResult.message(), queryRuntimeResult.status().name());
        }
    }
}
