package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.ConversationState;
import com.hojak99.travelaiagent.chat.domain.QueryRuntimeResult;
import com.hojak99.travelaiagent.chat.domain.RuntimeStatus;
import com.hojak99.travelaiagent.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryRuntimeService {
    private static final int MAX_ITERATIONS = 3;
    private static final String INITIAL_INSTRUCTIONS = """
            당신은 사용자의 여행 계획을 함께 구체화하는 대화형 여행 Agent입니다.
            지금은 여행 계획을 바로 만들지 말고, 대화 기록에서 아직 확인되지 않은 핵심 조건을 파악하세요.
            이미 사용자가 답한 내용을 다시 묻지 말고, 가장 중요한 추가 질문을 한 번에 최대 2개만 자연스럽게 하세요.
            답변은 한국어로 작성하세요.
            """;

    private final LlmClient llmClient;

    public QueryRuntimeResult run(ConversationState conversationState, String message) {
        conversationState.addUserMessage(message);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            conversationState.startIteration();

            if (!conversationState.hasRequiredTravelInformation()) {
                String question = llmClient.generate(INITIAL_INSTRUCTIONS, conversationState.getMessages());
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
