package com.hojak99.travelaiagent.chat.controller;

import jakarta.validation.Valid;
import com.hojak99.travelaiagent.chat.controller.request.ChatRequest;
import com.hojak99.travelaiagent.chat.controller.response.ChatResponse;
import com.hojak99.travelaiagent.chat.domain.QueryCommand;
import com.hojak99.travelaiagent.chat.domain.QueryResult;
import com.hojak99.travelaiagent.chat.service.QueryEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat")
public class ChatController {


    private final QueryEngineService queryEngineService;

    /**
     * Controller는 State 관리, LLM 호출, Tool 실행을 직접 담당하지 않는다.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        QueryCommand queryCommand = new QueryCommand(request.sessionId(), request.message());
        QueryResult queryResult = queryEngineService.submit(queryCommand);
        return new ChatResponse(queryResult.sessionId(), queryResult.message(), queryResult.status());
    }
}
