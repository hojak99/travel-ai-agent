package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.ConversationMessage;
import com.hojak99.travelaiagent.chat.domain.QueryCommand;
import com.hojak99.travelaiagent.chat.domain.QueryResult;
import com.hojak99.travelaiagent.chat.repository.ConversationStateRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryEngineServiceTest {

    private final ConversationStateRepository repository = new ConversationStateRepository();
    private final QueryEngineService queryEngine = new QueryEngineService(
            repository,
            new QueryRuntimeService((instructions, messages) -> "여행 날짜와 예산을 알려주세요."));

    @Test
    void keepsConversationStateForSameSession() {
        queryEngine.submit(new QueryCommand("study-1", "여행 일정 추천해줘"));
        QueryResult secondResult = queryEngine.submit(new QueryCommand("study-1", "오사카로 가고 싶어"));

        var state = repository.loadOrCreate("study-1");
        assertThat(secondResult.status()).isEqualTo("NEED_USER_INPUT");
        assertThat(state.getMessages()).extracting(ConversationMessage::role)
                .containsExactly(ConversationMessage.Role.USER, ConversationMessage.Role.ASSISTANT,
                        ConversationMessage.Role.USER, ConversationMessage.Role.ASSISTANT);
    }

    @Test
    void createsSeparateStateForDifferentSessions() {
        queryEngine.submit(new QueryCommand("study-1", "오사카"));
        queryEngine.submit(new QueryCommand("study-2", "도쿄"));

        assertThat(repository.loadOrCreate("study-1").getMessages()).hasSize(2);
        assertThat(repository.loadOrCreate("study-2").getMessages()).hasSize(2);
    }

    @Test
    void getsCurrentStateWithoutCreatingUnknownSession() {
        queryEngine.submit(new QueryCommand("study-1", "여행 일정을 추천해줘"));

        var state = queryEngine.getState("study-1");

        assertThat(state.sessionId()).isEqualTo("study-1");
        assertThat(state.messages()).hasSize(2);
        assertThat(state.pendingQuestion()).isNotBlank();
        assertThat(state.iteration()).isEqualTo(1);
        assertThatThrownBy(() -> queryEngine.getState("unknown"))
                .isInstanceOf(SessionNotFoundException.class);
    }
}
