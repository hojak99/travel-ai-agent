package com.hojak99.travelaiagent.chat.domain;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConversationState {
    private String sessionId;
    private List<ConversationMessage> messages;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> travelers;
    private String budget;
    private List<String> preferences;
    private List<String> confirmActivities;
    private String pendingQuestion;

    public static ConversationState create(String sessionId) {
        return ConversationState.builder()
                .sessionId(sessionId)
                .messages(new ArrayList<>())
                .travelers(new ArrayList<>())
                .preferences(new ArrayList<>())
                .confirmActivities(new ArrayList<>())
                .build();
    }

    public void addUserMessage(String message) {
        this.messages.add(new ConversationMessage(ConversationMessage.Role.USER, message));
    }

    public void addAssistantMessage(String message) {
        this.messages.add(new ConversationMessage(ConversationMessage.Role.ASSISTANT, message));
    }
}
