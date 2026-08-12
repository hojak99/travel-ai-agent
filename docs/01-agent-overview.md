# 01. QueryEngine과 한 번의 Agent 실행

## 이 문서의 목적

이번 문서는 Spring 계층 설계나 코드 스타일을 공부하기 위한 문서가 아니다. Claude Code에서 대화 한 번이 Agent Runtime을 통과하는 방식을 여행 Agent에 적용해보며, QueryEngine과 TAO Loop의 관계를 이해하는 것이 목적이다.

이번 단계에서는 사용자가 여행 계획을 요청했을 때 Agent가 어떤 상태를 읽고, 어떤 판단을 내리고, 다음 행동을 선택하는지 설계한다.

## 여행 Agent의 실행 흐름

```text
사용자 메시지
    ↓
QueryEngine
    ↓
현재 여행 State와 대화 Context 확인
    ↓
Think: 무엇이 부족하고 다음 행동은 무엇인가?
    ↓
Act: 질문, Tool, Sub-agent, 최종 응답 중 하나 선택
    ↓
Observe: 사용자 답변 또는 실행 결과 수신
    ↓
State 갱신
    ↓
종료 또는 다음 Loop
```

QueryEngine은 이 실행을 시작하고 관리하는 Agent Runtime의 진입점이다. 단순히 사용자 메시지를 LLM에 전달하는 함수가 아니다.

## QueryEngine이 해결하는 문제

사용자가 처음부터 모든 여행 조건을 주지는 않는다.

```text
사용자: 여행 일정 추천해줘
Agent: 어디로 가고 싶으신가요?
사용자: 오사카
Agent: 여행 기간과 예산은 어떻게 되나요?
사용자: 3박 4일, 150만원
```

QueryEngine은 각각의 사용자 입력을 독립적인 요청으로 보지 않고 하나의 여행 Session에 연결한다. 매번 현재까지의 State를 읽고, 새로운 답변을 State에 반영한 뒤, 다음 행동을 결정할 수 있도록 Runtime을 실행한다.

## QueryEngine과 QueryRuntime

### QueryEngine

여행 Session의 전체 실행을 관리하는 진입점이다.

- Session을 식별한다.
- 현재 여행 State를 불러온다.
- 새 사용자 메시지를 현재 Session에 연결한다.
- 한 번의 Agent 실행을 시작한다.
- 실행 중 발생한 결과와 이벤트를 외부 채널로 전달한다.
- 다음 사용자 입력을 기다릴지, 작업을 계속할지, 최종 결과를 반환할지 결정한다.

### QueryRuntime

현재 사용자 입력을 처리하는 실행 단위다.

- 현재 State와 Context를 읽는다.
- LLM에게 판단을 요청한다.
- 판단 결과에 따라 질문·Tool·Sub-agent·최종 응답을 실행한다.
- 실행 결과를 State에 반영한다.
- 아직 작업이 끝나지 않았으면 다음 TAO Loop를 계속한다.

QueryEngine이 Session 전체를 관리한다면, QueryRuntime은 그 Session 안에서 현재 요청을 처리하는 Agent의 작업 현장이다.

## Agent Decision

LLM의 응답은 단순한 자연어가 아니라 Agent가 다음 행동을 선택한 결과로 해석한다.

```text
ASK_USER   필요한 여행 조건을 사용자에게 질문
CALL_TOOL  Local Tool 또는 MCP Tool 실행
DELEGATE   전문 Sub-agent에게 작업 위임
FINAL      여행 일정 또는 설명을 최종 반환
ERROR      복구할 수 없는 오류 반환
```

예를 들어 첫 메시지에 대한 내부 판단은 다음과 같다.

```json
{
  "action": "ASK_USER",
  "missingFields": ["destination", "dates", "budget"],
  "question": "어디로, 며칠 동안, 어느 정도 예산으로 여행하시나요?"
}
```

사용자 답변이 State에 충분히 쌓이면 판단은 다음처럼 바뀐다.

```json
{
  "action": "CALL_TOOL",
  "tool": "search_places",
  "reason": "여행 조건이 충분하므로 장소 후보를 검색한다"
}
```

## 여행 State

대화 원문과 현재까지 파악된 여행 조건을 함께 관리한다. 대화 원문은 사용자가 실제로 말한 사실을 보존하고, 구조화 State는 Agent가 판단과 Tool 실행에 사용한다.

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

예를 들어 사용자가 “아이와 함께 가고, 오래 걷는 건 싫어요”라고 말하면 원문 메시지는 보존하고, 구조화 State에는 다음과 같이 반영한다.

```text
travelers: 가족
preferences: 어린이 친화적 장소
avoid: 장시간 도보
```

이 구분이 있어야 이후 Compaction을 하더라도 중요한 여행 제약 조건을 잃지 않는다.

## 이번 단계에서 확인할 Agent 시나리오

### 시나리오 1: 정보가 부족한 경우

```text
입력: 여행 일정 추천해줘
판단: 목적지·기간·예산이 없음
행동: ASK_USER
관찰: 사용자의 추가 답변을 기다림
상태: pendingQuestion과 답변을 저장
```

### 시나리오 2: 정보가 충분해진 경우

```text
입력: 오사카, 3박 4일, 2명, 예산 150만원
판단: 장소 검색과 이동 정보가 필요함
행동: CALL_TOOL 또는 DELEGATE
관찰: 검색·Sub-agent 결과 수신
상태: 후보 장소와 검증 결과를 저장
```

### 시나리오 3: 일정이 완성된 경우

```text
판단: 일정 생성과 검증이 끝남
행동: FINAL
결과: 구조화된 ItineraryOutput과 자연어 설명 반환
```

## 이번 단계의 개발 목표

이번 단계에서는 위 흐름을 실행할 수 있는 최소 Runtime을 만든다.

- Session별로 ConversationState를 유지한다.
- 새 사용자 메시지를 기존 Session State에 추가한다.
- Runtime이 임시 Agent Decision 또는 응답을 반환한다.
- 같은 Session의 다음 입력이 이전 State를 바탕으로 처리되는지 확인한다.
- Controller와 저장소의 구현 방식보다 Agent의 입력·판단·행동·관찰·상태 갱신 흐름을 확인한다.

실제 LLM, Tool, MCP, Sub-agent는 다음 단계에서 추가한다. 지금은 TAO Loop가 들어갈 자리를 만들고, 이후 기능들이 어느 지점에서 실행되는지 이해하는 것이 목표다.

## 완료 기준

다음 대화가 하나의 Session 안에서 연결되면 01단계가 완료된 것이다.

```text
사용자: 여행 일정 추천해줘
Agent: 목적지와 기간을 알려주세요.
사용자: 오사카 3박 4일
Agent: 예산과 동행자를 알려주세요.
```

두 번째 사용자 입력이 첫 번째 입력과 별개의 새 대화가 아니라, 동일한 여행 State를 확장해야 한다.

## 원본 Claude Code와의 대응

```text
Claude Code QueryEngine  → 여행 Session의 Agent 실행 진입점
Claude Code query Kernel → 한 번의 여행 요청을 처리하는 QueryRuntime
Conversation State       → 여행 조건과 대화 원문
Tool Loop                → 질문·Tool·Sub-agent를 반복 실행하는 TAO Loop
Stream Events            → React에 Agent 진행 상태를 전달하는 이벤트
```

다음 문서에서는 이 State를 더 구체화하고, 그다음 문서에서 LLM에 어떤 Context와 메시지를 전달해야 하는지 다룬다.
