package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.controller.response.ConversationStateResponse;
import com.hojak99.travelaiagent.chat.domain.ConversationState;
import com.hojak99.travelaiagent.chat.domain.QueryCommand;
import com.hojak99.travelaiagent.chat.domain.QueryResult;
import com.hojak99.travelaiagent.chat.domain.QueryRuntimeResult;
import com.hojak99.travelaiagent.chat.domain.RuntimeCancellationSignal;
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
    private final RuntimeCancellationRegistry cancellationRegistry;

    /**
     * 동일 Session의 실행을 직렬화해 메시지와 State 갱신 순서를 보존한다.
     */
    public QueryResult submit(QueryCommand queryCommand) {
        ConversationState conversationState = conversationStateRepository.loadOrCreate(queryCommand.sessionId());
        synchronized (conversationState) {
            RuntimeCancellationSignal cancellationSignal =
                    cancellationRegistry.start(queryCommand.sessionId());
            try {
                QueryRuntimeResult queryRuntimeResult = queryRuntimeService.run(
                        conversationState,
                        queryCommand.message(),
                        cancellationSignal
                );
                return new QueryResult(
                        conversationState.getSessionId(),
                        queryRuntimeResult.message(),
                        queryRuntimeResult.status().name()
                );
            } finally {
                cancellationRegistry.finish(queryCommand.sessionId(), cancellationSignal);
            }
        }
    }

    /**
     * Runtime 잠금을 기다리지 않고 현재 활성 실행에 취소 신호를 전달한다.
     */
    public boolean cancel(String sessionId) {
        return cancellationRegistry.cancel(sessionId);
    }

    /**
     * Domain 객체를 노출하지 않고 일관된 State snapshot을 반환한다.
     */
    public ConversationStateResponse getState(String sessionId) {
        ConversationState conversationState = conversationStateRepository.find(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        synchronized (conversationState) {
            return ConversationStateResponse.from(conversationState);
        }
    }
}
