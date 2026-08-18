package com.hojak99.travelaiagent.chat.controller;

import jakarta.validation.Valid;
import com.hojak99.travelaiagent.chat.controller.request.ChatRequest;
import com.hojak99.travelaiagent.chat.controller.response.ChatResponse;
import com.hojak99.travelaiagent.chat.controller.response.ConversationStateResponse;
import com.hojak99.travelaiagent.chat.domain.QueryCommand;
import com.hojak99.travelaiagent.chat.domain.QueryResult;
import com.hojak99.travelaiagent.chat.service.QueryEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅 HTTP 계약을 QueryEngine의 세션 실행·조회·취소 기능에 연결한다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat")
public class ChatController {


    private final QueryEngineService queryEngineService;

    /**
     * 사용자 메시지를 같은 sessionId의 Agent 실행으로 전달한다.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        QueryCommand queryCommand = new QueryCommand(request.sessionId(), request.message());
        QueryResult queryResult = queryEngineService.submit(queryCommand);
        return new ChatResponse(queryResult.sessionId(), queryResult.message(), queryResult.status());
    }

    /**
     * 세션을 새로 만들지 않고 현재까지 누적된 State를 조회한다.
     */
    @GetMapping(value = "/{sessionId}/state", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConversationStateResponse state(@PathVariable String sessionId) {
        return queryEngineService.getState(sessionId);
    }
}
