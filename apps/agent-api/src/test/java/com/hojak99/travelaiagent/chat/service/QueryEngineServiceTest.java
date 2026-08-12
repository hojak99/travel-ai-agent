package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.ConversationMessage;
import com.hojak99.travelaiagent.chat.domain.QueryCommand;
import com.hojak99.travelaiagent.chat.domain.QueryResult;
import com.hojak99.travelaiagent.chat.repository.ConversationStateRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryEngineServiceTest {

    private final ConversationStateRepository repository = new ConversationStateRepository();
    private final QueryEngineService queryEngine = new QueryEngineService(repository, new QueryRuntimeService());

    @Test
    void keepsConversationStateForSameSession() {
        queryEngine.submit(new QueryCommand("study-1", "?ы뻾 ?쇱젙 異붿쿇?댁쨾"));
        QueryResult secondResult = queryEngine.submit(new QueryCommand("study-1", "?ㅼ궗移대줈 媛怨??띠뼱"));

        var state = repository.loadOrCreate("study-1");
        assertThat(secondResult.status()).isEqualTo("COMPLETED");
        assertThat(state.getMessages()).extracting(ConversationMessage::role)
                .containsExactly(ConversationMessage.Role.USER, ConversationMessage.Role.ASSISTANT,
                        ConversationMessage.Role.USER, ConversationMessage.Role.ASSISTANT);
    }

    @Test
    void createsSeparateStateForDifferentSessions() {
        queryEngine.submit(new QueryCommand("study-1", "?ㅼ궗移?"));
        queryEngine.submit(new QueryCommand("study-2", "?꾩퓙"));


        assertThat(repository.loadOrCreate("study-1").getMessages()).hasSize(2);
        assertThat(repository.loadOrCreate("study-2").getMessages()).hasSize(2);
    }
}

