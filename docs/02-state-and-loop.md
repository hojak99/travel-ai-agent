# 02. Travel State와 TAO 실행 Loop

## 이 문서의 목적

01단계에서 Session과 Runtime의 경계를 만들었다. 02단계에서는 Agent가 현재 여행 상태를 읽고, 한 번의 사용자 입력을 처리한 뒤 다음 행동을 결정하는 실행 Loop를 넣는다.

이번 단계의 초점은 자연어를 완벽하게 해석하는 것이 아니다. `State → 판단 → 행동 → 결과 → State 갱신`이라는 Agent 실행 단위를 코드로 확인하는 것이다.

## Agent Loop

```text
사용자 메시지 수신
    ↓
State에 사용자 메시지 추가
    ↓
Think: 필수 여행 조건이 충분한가?
    ↓
Act: 질문 또는 최종 단계 진입
    ↓
Observe: 질문 결과 또는 Tool 결과를 받음
    ↓
State 갱신
    ↓
다음 사용자 입력 또는 다음 Loop
```

현재는 LLM이 Think를 수행하지 않는다. `ConversationState.hasRequiredTravelInformation()`이 임시 판단기 역할을 한다. 03단계에서 이 위치를 실제 LLM Decision으로 교체한다.

## Travel State

`ConversationState`는 두 종류의 정보를 보존한다.

### 대화 원문

사용자와 Agent가 실제로 주고받은 메시지다. Compaction과 감사 로그의 원본이 된다.

```text
USER: 여행 일정 추천해줘
ASSISTANT: 여행지, 여행 날짜, 예산을 알려주세요.
```

메시지에는 `USER`와 `ASSISTANT` 역할을 함께 저장해야 이후 Prompt 구성에서 순서를 잃지 않는다.

### 구조화된 여행 조건

Agent의 판단과 Tool 입력에 사용하는 현재 값이다.

```text
destination
startDate
endDate
travelers
budget
preferences
confirmedActivities
pendingQuestion
```

메시지 원문이 있다고 해서 구조화 State가 자동으로 채워지는 것은 아니다. 이 변환은 03단계에서 LLM 또는 별도 Requirement Extractor가 담당한다.

## 현재 구현된 상태 전이

```text
새 사용자 메시지
    ↓
USER 메시지 저장
    ↓
필수 조건 확인
    ├─ 부족함
    │    ├─ pendingQuestion 저장
    │    ├─ ASSISTANT 질문 저장
    │    └─ NEED_USER_INPUT 반환
    │
    └─ 충분함
         ├─ pendingQuestion 제거
         ├─ 다음 일정 생성 단계 안내
         └─ FINAL 반환
```

현재 필수 조건은 목적지, 시작일, 종료일, 예산이다. 실제 값 추출은 아직 구현하지 않았기 때문에 새 세션은 `NEED_USER_INPUT`으로 끝난다. 이것은 실패가 아니라 다음 사용자 입력을 기다리는 정상적인 Agent 종료 상태다.

## RuntimeStatus

```text
NEED_USER_INPUT  추가 정보를 기다리는 정상 상태
FINAL            현재 실행에서 최종 결과를 반환한 상태
MAX_ITERATIONS   한 번의 실행이 허용된 반복 횟수를 넘은 상태
ERROR            복구하지 못한 실행 오류
```

`NEED_USER_INPUT`은 오류가 아니다. React는 이 상태를 받으면 질문을 대화 화면에 표시하고, 사용자의 다음 메시지를 같은 Session으로 다시 QueryEngine에 전달해야 한다.

## 반복 횟수 제한

Agent Loop에는 반드시 상한이 있어야 한다.

```text
for (i = 0; i < MAX_ITERATIONS; i++) {
    Decision 생성
    Action 실행
    Observation 반영
    종료 조건 확인
}
```

LLM이 계속 Tool을 호출하거나 같은 질문을 반복하는 상황을 막기 위해 `MAX_ITERATIONS`를 둔다. 상한에 도달하면 `MAX_ITERATIONS`를 반환하고, 무한 실행하지 않는다.

## 이번 구현에서 확인할 시나리오

### 정보가 부족한 세션

```text
입력: 여행 일정 추천해줘
State: destination/startDate/endDate/budget 없음
결과: 질문 반환 + NEED_USER_INPUT
```

### 같은 Session의 후속 입력

```text
입력 1: 여행 일정 추천해줘
입력 2: 오사카로 가고 싶어
결과: 두 입력이 같은 ConversationState의 messages에 순서대로 저장
```

다만 현재는 `오사카`라는 자연어에서 destination을 추출하지 않는다. 따라서 두 번째 입력만으로 `FINAL`이 되지 않는다. 이 동작은 다음 단계의 Requirement Extraction 과제다.

## 이번 단계에서 구현한 것

- `RuntimeStatus`로 Agent 실행 종료 상태를 구조화했다.
- `ConversationState`에 iteration과 pendingQuestion을 추가했다.
- Runtime이 현재 State를 읽고 `NEED_USER_INPUT` 또는 `FINAL`을 반환한다.
- 사용자 질문과 Agent 응답을 역할과 함께 State에 저장한다.
- `MAX_ITERATIONS` 상한을 Runtime에 두었다.
- QueryEngine이 Runtime 결과의 상태를 API 응답까지 전달한다.

## 아직 구현하지 않은 것

- LLM을 이용한 Think
- 사용자 메시지에서 여행 조건 추출
- 실제 `CALL_TOOL`과 `DELEGATE`
- Tool 결과를 Observation으로 반영
- Compaction과 Checkpoint

## 완료 기준

다음 질문에 답할 수 있으면 02단계의 Agent 개념을 이해한 것이다.

1. `NEED_USER_INPUT`이 오류가 아닌 이유는 무엇인가?
2. State에 메시지 원문과 구조화 조건을 따로 저장하는 이유는 무엇인가?
3. 다음 사용자 메시지가 이전 State와 연결되는 지점은 어디인가?
4. LLM이 계속 행동을 반복할 때 Runtime을 어떻게 멈추는가?
5. 현재 구현에서 자연어 여행 조건 추출은 왜 아직 되지 않는가?

## 다음 단계

03단계에서 현재 임시 판단기를 LLM 기반 `AgentDecision`으로 교체한다.

```text
ConversationState
→ ContextManager
→ PromptBuilder
→ LLM
→ AgentDecision
→ ASK_USER / CALL_TOOL / DELEGATE / FINAL
```
