# 01. 여행 Agent 전체 구조와 첫 번째 대화 흐름

## 학습 목표

원본 01장과 05장의 관점을 여행 Agent에 적용한다. 외부 요청을 받는 Facade와 한 번의 실행을 담당하는 Runtime을 분리하고, 사용자의 정보가 부족할 때 질문을 반환하는 기본 생명주기를 설계한다.

## 목표 흐름

```text
React Chat UI
    -> ChatController
    -> QueryEngine
    -> QueryRuntime
    -> ContextManager
    -> LLM Decision
    -> ASK_USER / CALL_TOOL / DELEGATE / FINAL
```

이번 단계에서는 실제 LLM, Tool, Sub-agent, MCP를 연결하지 않는다. 먼저 요청과 실행의 경계를 설계하고, 각 컴포넌트의 책임을 문서와 인터페이스로 고정한다.

## 핵심 개념

### ChatController

HTTP 입구다. 인증, 요청 검증, 스트리밍 연결을 담당하고 Agent 내부 실행 순서는 알지 못해야 한다.

### QueryEngine

세션 단위 Facade다. 대화 세션을 찾고, 사용자 입력을 QueryRuntime에 전달하고, Runtime이 발행한 이벤트와 종료 결과를 외부 API 형식으로 변환한다.

### QueryRuntime

한 번의 사용자 입력을 처리하는 실행 단위다. State를 읽고 Context를 구성하고 LLM과 Tool을 반복 호출한다. Runtime이 끝나도 세션의 영속 State는 StateStore에 남는다.

### ConversationState

대화 원문과 구조화된 여행 조건을 포함하는 현재 상태다. 다음 필드를 단계적으로 추가한다.

```text
sessionId
messages
destination
startDate
endDate
travelers
budget
preferences
confirmedActivities
pendingQuestion
```

### LlmClient

모델 호출 경계다. QueryRuntime이 특정 LLM SDK에 직접 의존하지 않도록 인터페이스로 분리한다. 모델 구현은 이후 단계에서 교체한다.

## 직접 구현 과제

- `ChatController`에서 `POST /api/chat` 요청 계약을 정의한다.
- `QueryEngine`을 만들고 `sessionId`로 세션을 조회한다.
- `QueryRuntime`이 State를 입력받아 `AgentDecision`을 반환하게 한다.
- `AgentDecision`의 `ASK_USER`, `CALL_TOOL`, `DELEGATE`, `FINAL`, `ERROR`를 정의한다.
- 같은 `sessionId`의 두 번째 요청에서 이전 State가 유지되는지 테스트한다.
- 외부 API 응답과 내부 Runtime 이벤트를 분리한다.

## 설계 질문

1. 왜 Controller가 LLM을 직접 호출하면 안 되는가?
2. 세션 상태와 한 번의 실행 상태는 무엇이 다른가?
3. 질문을 반환한 뒤 다음 사용자 답변은 어느 State에 합쳐지는가?
4. Runtime이 중단되면 어디까지 실행됐는지 어떻게 복구할 것인가?

## 원본과의 대응

원본 05장의 `QueryEngine` Facade, `query()` Kernel, Query Config/Deps/Stop Hook/Token Budget의 분리를 여행 Agent의 `QueryEngine`, `QueryRuntime`, `ContextManager`, `ExecutionPolicy`로 대응시킨다.
