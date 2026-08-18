package com.hojak99.travelaiagent.llm.domain;

import java.util.List;

/**
 * Responses API output 항목의 종류와 content 목록을 보존한다.
 */
public record OpenAiOutputItem(String type, List<OpenAiOutputContent> content) {
}
