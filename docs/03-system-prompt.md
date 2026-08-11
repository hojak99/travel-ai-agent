# 03. System Prompt와 출력 형식

System Prompt는 Agent의 역할, 제한, 사용 가능한 Tool, 응답 형식을 정의한다.

```text
역할 → 여행 규칙 → 현재 State → Tool 설명 → 출력 schema → 사용자 메시지
```

## 여행 Agent 규칙 예시

- 확인하지 않은 가격을 확정 가격처럼 말하지 않는다.
- 영업시간과 이동 시간은 Tool 결과를 우선한다.
- 예산에는 통화와 환율 기준 시각을 표시한다.
- 정보가 부족하면 필요한 질문을 모아 묻는다.

## 직접 구현 과제

- `PromptBuilder`를 만들고 State를 입력으로 받는다.
- LLM 출력은 `AssistantDecision`으로 파싱한다.
- `FINAL`, `CALL_TOOL`, `ASK_USER`를 구분한다.
- 파싱 실패 시 재시도 또는 불확실성 응답을 설계한다.
