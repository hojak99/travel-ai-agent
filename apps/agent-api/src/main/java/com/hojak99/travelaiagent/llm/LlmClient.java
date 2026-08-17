package com.hojak99.travelaiagent.llm;

import com.hojak99.travelaiagent.chat.domain.ConversationMessage;

import java.util.List;

@FunctionalInterface
public interface LlmClient {

    String generate(String instructions, List<ConversationMessage> messages);
}
