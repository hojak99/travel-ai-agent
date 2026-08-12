package com.hojak99.travelaiagent.chat.domain;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConversationState {

    private String sessionId;
    private List<String> messages;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> travelers;
    private String budget;
    private List<String> preferences;
    private List<String> confirmActivities;
    private String pendingQuestion;
}
