package com.hojak99.travelaiagent.llm;

import com.hojak99.travelaiagent.chat.domain.ConversationMessage;

import java.util.List;

/**
 * Runtime이 특정 LLM 공급자와 HTTP 계약에 의존하지 않도록 하는 생성 경계다.
 */
@FunctionalInterface
public interface LlmClient {

    /**
     * System 지침과 역할이 보존된 대화로 다음 Agent 응답을 생성한다.
     */
    String generate(String instructions, List<ConversationMessage> messages);
}
