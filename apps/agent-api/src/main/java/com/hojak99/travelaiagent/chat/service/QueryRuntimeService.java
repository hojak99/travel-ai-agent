package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.ConversationState;
import com.hojak99.travelaiagent.chat.domain.QueryRuntimeResult;
import com.hojak99.travelaiagent.chat.domain.RuntimeStatus;
import org.springframework.stereotype.Service;

@Service
public class QueryRuntimeService {
    private static final int MAX_ITERATIONS = 3;

    public QueryRuntimeResult run(ConversationState conversationState, String message) {
        conversationState.addUserMessage(message);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            conversationState.startIteration();

            if (!conversationState.hasRequiredTravelInformation()) {
                String question = "여행지, 여행 날짜, 예산을 알려주세요.";
                conversationState.setPendingQuestion(question);
                conversationState.addAssistantMessage(question);
                return new QueryRuntimeResult(question, RuntimeStatus.NEED_USER_INPUT, i + 1);
            }

            conversationState.setPendingQuestion(null);
            String result = "여행 조건이 충분합니다. 다음 단계에서 일정을 생성합니다.";
            conversationState.addAssistantMessage(result);
            return new QueryRuntimeResult(result, RuntimeStatus.FINAL, i + 1);
        }

        String result = "이번 실행에서 처리할 수 있는 최대 단계에 도달했습니다.";
        conversationState.addAssistantMessage(result);
        return new QueryRuntimeResult(result, RuntimeStatus.MAX_ITERATIONS, MAX_ITERATIONS);
    }
}
