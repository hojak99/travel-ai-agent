package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.ConversationState;
import com.hojak99.travelaiagent.chat.domain.QueryCommand;
import com.hojak99.travelaiagent.chat.domain.QueryResult;
import com.hojak99.travelaiagent.chat.domain.QueryRuntimeResult;
import com.hojak99.travelaiagent.chat.controller.response.ConversationStateResponse;
import com.hojak99.travelaiagent.chat.repository.ConversationStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Session State와 한 번의 QueryRuntime 실행 사이를 조정하는 Agent 진입점이다.
 */
@Service
@RequiredArgsConstructor
public class QueryEngineService {

    private final ConversationStateRepository conversationStateRepository;
    private final QueryRuntimeService queryRuntimeService;

    /**
     * 동일 Session의 실행을 직렬화해 메시지와 State 갱신 순서를 보존한다.
     */
    public QueryResult submit(QueryCommand queryCommand) {
        ConversationState conversationState = conversationStateRepository.loadOrCreate(queryCommand.sessionId());
        synchronized (conversationState) {
            QueryRuntimeResult queryRuntimeResult = queryRuntimeService.run(conversationState, queryCommand.message());
            return new QueryResult(conversationState.getSessionId(), queryRuntimeResult.message(), queryRuntimeResult.status().name());
        }
    }

    public ConversationStateResponse getState(String sessionId) {
        ConversationState conversationState = conversationStateRepository.find(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        synchronized (conversationState) {
            return ConversationStateResponse.from(conversationState);
        }
    }
}
