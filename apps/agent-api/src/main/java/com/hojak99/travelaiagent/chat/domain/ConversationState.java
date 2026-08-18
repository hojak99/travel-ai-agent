package com.hojak99.travelaiagent.chat.domain;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 한 여행 Session의 대화 원문과 구조화 조건, Runtime 진행 상태를 함께 보존한다.
 */
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
    private int iteration;

    public static ConversationState create(String sessionId) {
        return ConversationState.builder().sessionId(sessionId).messages(new ArrayList<>()).travelers(new ArrayList<>()).preferences(new ArrayList<>()).confirmActivities(new ArrayList<>()).iteration(0).build();
    }

    public void addUserMessage(String message) {
        messages.add(new ConversationMessage(ConversationMessage.Role.USER, message));
    }

    public void addAssistantMessage(String message) {
        messages.add(new ConversationMessage(ConversationMessage.Role.ASSISTANT, message));
    }

    /**
     * 한 번의 Runtime 판단 시도를 누적해 반복 상한과 상태 조회에 사용한다.
     */
    public void startIteration() {
        iteration++;
    }

    public int iteration() {
        return iteration;
    }

    /**
     * 일정 생성 단계로 넘어가기 위한 최소 여행 조건이 모였는지 판정한다.
     */
    public boolean hasRequiredTravelInformation() {
        return destination != null && !destination.isBlank() && startDate != null && endDate != null && budget != null && !budget.isBlank();
    }

    public void setPendingQuestion(String pendingQuestion) {
        this.pendingQuestion = pendingQuestion;
    }
}
