package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.ConversationState;
import com.hojak99.travelaiagent.chat.domain.QueryRuntimeResult;
import com.hojak99.travelaiagent.chat.domain.RuntimeCancellationSignal;
import com.hojak99.travelaiagent.chat.domain.RuntimeStatus;
import com.hojak99.travelaiagent.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 한 사용자 입력에서 판단·종료 상태를 만들고 그 결과를 ConversationState에 반영한다.
 */
@Service
@RequiredArgsConstructor
public class QueryRuntimeService {
    private static final int MAX_ITERATIONS = 3;
    private static final String CANCELLED_MESSAGE = "요청이 취소되었습니다.";
    private static final String ERROR_MESSAGE = "여행 계획을 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    private static final String MAX_ITERATIONS_MESSAGE = "유효한 응답을 만들지 못해 이번 실행을 중단했습니다.";
    private static final String INITIAL_INSTRUCTIONS = """
            당신은 사용자의 여행 계획을 함께 구체화하는 대화형 여행 Agent입니다.
            지금은 여행 계획을 바로 만들지 말고, 대화 기록에서 아직 확인되지 않은 핵심 조건을 파악하세요.
            이미 사용자가 답한 내용을 다시 묻지 말고, 가장 중요한 추가 질문을 한 번에 최대 2개만 자연스럽게 하세요.
            답변은 한국어로 작성하세요.
            """;

    private final LlmClient llmClient;

    /**
     * 빈 LLM 응답은 제한적으로 재시도하고 취소·오류·사용자 대기를 명시적으로 종료한다.
     */
    public QueryRuntimeResult run(
            ConversationState conversationState,
            String message,
            RuntimeCancellationSignal cancellationSignal
    ) {
        conversationState.addUserMessage(message);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            conversationState.startIteration();

            if (cancellationSignal.isCancelled()) {
                return finish(conversationState, CANCELLED_MESSAGE, RuntimeStatus.CANCELLED, i + 1);
            }

            if (!conversationState.hasRequiredTravelInformation()) {
                String question;
                try {
                    question = llmClient.generate(INITIAL_INSTRUCTIONS, conversationState.getMessages());
                } catch (RuntimeException exception) {
                    return finish(conversationState, ERROR_MESSAGE, RuntimeStatus.ERROR, i + 1);
                }

                if (cancellationSignal.isCancelled()) {
                    return finish(conversationState, CANCELLED_MESSAGE, RuntimeStatus.CANCELLED, i + 1);
                }

                if (question == null || question.isBlank()) {
                    continue;
                }

                conversationState.setPendingQuestion(question);
                conversationState.addAssistantMessage(question);
                return new QueryRuntimeResult(question, RuntimeStatus.NEED_USER_INPUT, i + 1);
            }

            conversationState.setPendingQuestion(null);
            String result = "여행 조건이 충분합니다. 다음 단계에서 일정을 생성합니다.";
            conversationState.addAssistantMessage(result);
            return new QueryRuntimeResult(result, RuntimeStatus.FINAL, i + 1);
        }

        return finish(conversationState, MAX_ITERATIONS_MESSAGE, RuntimeStatus.MAX_ITERATIONS, MAX_ITERATIONS);
    }

    /**
     * 모든 비정상 종료가 pendingQuestion과 대화 이력을 같은 방식으로 정리하게 한다.
     */
    private QueryRuntimeResult finish(
            ConversationState conversationState,
            String message,
            RuntimeStatus status,
            int iterations
    ) {
        conversationState.setPendingQuestion(null);
        conversationState.addAssistantMessage(message);
        return new QueryRuntimeResult(message, status, iterations);
    }
}
