# 02. State와 대화 루프

## 핵심 개념

Agent는 한 번의 LLM 호출이 아니라 상태를 읽고, 메시지를 추가하고, 다음 행동을 결정하는 반복 실행이다.

```text
요청 수신 → State 로드 → 메시지 구성 → LLM 호출
→ Tool 호출 여부 판단 → State 갱신 → 종료 또는 다음 Loop
```

## 여행 State 초안

```text
sessionId, messages, destination, startDate, endDate
travelers, budget, preferences, confirmedActivities, pendingQuestion
```

대화 메시지와 여행 계획 필드는 분리한다. 메시지는 원본이고 여행 필드는 현재까지 추출된 구조화 상태다.

## 직접 구현 과제

- `ConversationState`를 정의한다.
- 같은 `sessionId`가 같은 State를 읽도록 만든다.
- 한 요청의 최대 Loop 횟수를 제한한다.
- 종료 이유를 `FINAL`, `NEED_USER_INPUT`, `MAX_ITERATIONS`, `ERROR`로 구분한다.
- State 갱신 단위 테스트를 작성한다.
