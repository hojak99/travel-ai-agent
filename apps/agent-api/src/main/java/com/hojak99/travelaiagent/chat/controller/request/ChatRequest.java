package com.hojak99.travelaiagent.chat.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 사용자가 한 번의 채팅 요청에서 보내는 외부 API 계약.
 */
public record ChatRequest(
        @NotBlank
        @Size(max = 128)
        @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._-]*")
        String sessionId,
        @NotBlank
        @Size(max = 10_000)
        String message
) {
}
