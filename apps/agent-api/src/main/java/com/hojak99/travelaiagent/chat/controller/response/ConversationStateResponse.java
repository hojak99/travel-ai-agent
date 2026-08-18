package com.hojak99.travelaiagent.chat.controller.response;

import com.hojak99.travelaiagent.chat.domain.ConversationMessage;
import com.hojak99.travelaiagent.chat.domain.ConversationState;

import java.time.LocalDate;
import java.util.List;

/**
 * 세션의 현재 여행 상태를 조회할 때 반환하는 외부 API 계약.
 */
public record ConversationStateResponse(
        String sessionId,
        List<ConversationMessage> messages,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        List<String> travelers,
        String budget,
        List<String> preferences,
        List<String> confirmActivities,
        String pendingQuestion,
        int iteration
) {
    public static ConversationStateResponse from(ConversationState state) {
        return new ConversationStateResponse(
                state.getSessionId(),
                List.copyOf(state.getMessages()),
                state.getDestination(),
                state.getStartDate(),
                state.getEndDate(),
                List.copyOf(state.getTravelers()),
                state.getBudget(),
                List.copyOf(state.getPreferences()),
                List.copyOf(state.getConfirmActivities()),
                state.getPendingQuestion(),
                state.getIteration()
        );
    }
}
