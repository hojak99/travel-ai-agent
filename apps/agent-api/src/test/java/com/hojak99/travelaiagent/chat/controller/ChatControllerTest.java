package com.hojak99.travelaiagent.chat.controller;

import com.hojak99.travelaiagent.chat.controller.response.ConversationStateResponse;
import com.hojak99.travelaiagent.chat.domain.ConversationMessage;
import com.hojak99.travelaiagent.chat.service.QueryEngineService;
import com.hojak99.travelaiagent.chat.service.SessionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {
    private QueryEngineService queryEngineService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        queryEngineService = mock(QueryEngineService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(queryEngineService)).build();
    }

    @Test
    void getsCurrentSessionState() throws Exception {
        when(queryEngineService.getState("study-1")).thenReturn(new ConversationStateResponse(
                "study-1",
                List.of(new ConversationMessage(ConversationMessage.Role.USER, "여행 일정 추천해줘")),
                null,
                null,
                null,
                List.of(),
                null,
                List.of(),
                List.of(),
                "목적지를 알려주세요.",
                1
        ));

        mockMvc.perform(get("/api/chat/study-1/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("study-1"))
                .andExpect(jsonPath("$.pendingQuestion").value("목적지를 알려주세요."))
                .andExpect(jsonPath("$.iteration").value(1));
    }

    @Test
    void returnsNotFoundForUnknownSession() throws Exception {
        when(queryEngineService.getState("unknown"))
                .thenThrow(new SessionNotFoundException("unknown"));

        mockMvc.perform(get("/api/chat/unknown/state"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelsAnActiveSessionRun() throws Exception {
        when(queryEngineService.cancel("study-1")).thenReturn(true);

        mockMvc.perform(post("/api/chat/study-1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("study-1"))
                .andExpect(jsonPath("$.cancelled").value(true));
    }

    @Test
    void rejectsInvalidChatRequest() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sessionId":"invalid session/id","message":""}
                                """))
                .andExpect(status().isBadRequest());
    }
}
