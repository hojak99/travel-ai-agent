package com.hojak99.travelaiagent.chat.domain;

public record ConversationMessage(Role role, String content) {
    public enum Role {
        USER,
        ASSISTANT
    }
}
