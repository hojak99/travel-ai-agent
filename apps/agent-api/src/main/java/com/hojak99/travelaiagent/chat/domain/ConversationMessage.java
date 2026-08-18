package com.hojak99.travelaiagent.chat.domain;

/**
 * Prompt 재구성에 필요한 발화 역할과 원문을 보존한다.
 */
public record ConversationMessage(Role role, String content) {
    public enum Role {
        USER,
        ASSISTANT
    }
}
