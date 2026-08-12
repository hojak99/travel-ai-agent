package com.hojak99.travelaiagent.chat.controller;

import com.hojak99.travelaiagent.chat.controller.request.ChatRequest;
import com.hojak99.travelaiagent.chat.controller.response.ChatResponse;
import com.hojak99.travelaiagent.chat.facade.QueryEngineFacade;
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


    private final QueryEngineFacade queryEngineFacade;

    /**
     * TODO: 요청을 QueryEngine Facade에 전달하고, 최종 ChatResponse로 변환한다.
     * Controller는 State 관리, LLM 호출, Tool 실행을 직접 담당하지 않는다.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@RequestBody ChatRequest request) {
        queryEngineFacade.query(request);

        // FIXME.
        return null;
    }
}
