# 15. 원본 한국어판 대조 결과와 보강 계획

## 검토 기준

원본 `Claude-Code-Source-Study/docs-kr`는 단순한 개념 목록이 아니다. 각 장이 다음 순서로 구성되어 있다.

```text
문제 정의
→ 실제 런타임 구조
→ 데이터와 이벤트 흐름
→ 실패·취소·재시도
→ 이식 가능한 설계 패턴
→ 다음 장과의 연결
```

우리 문서도 이 형식을 따라야 한다. 현재 문서에는 개념과 구현 과제는 있지만, 원본에 비해 런타임 흐름·실패 경로·컴포넌트 간 계약이 얕았다.

## 누락되었거나 보강해야 하는 원본 개념

### 1. QueryEngine Facade와 query Kernel

원본 05장은 세 층을 분리한다.

```text
QueryEngine Facade
    → query Kernel
        → query/config
        → query/deps
        → query/stopHooks
        → query/tokenBudget
```

여행 Agent에서는 `QueryEngine`이 세션 수준 상태와 API 변환을 맡고, `QueryRuntime`이 한 번의 사용자 입력에 대한 실행 Loop를 맡는다. Controller가 이 로직을 모두 갖지 않도록 한다.

### 2. Prompt의 섹션 조립과 Cache 경계

원본 06장과 08장은 System Prompt를 하나의 긴 문자열로 관리하지 않는다. 고정 섹션, 동적 섹션, Output Style, Tool Schema를 조립하고 캐시 가능한 경계를 구분한다.

여행 Agent의 Prompt는 다음처럼 나눈다.

```text
[캐시 가능]
여행 Agent 역할
여행 도메인 규칙
출력 Decision Schema
Tool 사용 규칙

[동적]
현재 Travel State
Compacted Summary
최근 대화
현재 사용자 메시지
```

### 3. Compaction은 한 가지 기능이 아니다

원본 07장에는 토큰 예산, 경고 상태, Microcompact, API 레벨 정리, Full Compact, Session Memory Compact, Post-Compact Cleanup이 분리되어 있다.

여행 Agent에서도 다음 단계가 필요하다.

```text
경고
→ 오래된 Tool 결과 정리
→ 대화 전체 요약
→ 구조화 State 재주입
→ 최근 메시지와 미해결 질문 복구
```

Compaction이 끝났다고 메시지 배열만 줄이면 안 된다. 압축 이후 다음 LLM 요청의 Context를 다시 조립해야 한다.

### 4. Tool Protocol과 ToolSearch

원본 10장은 Tool을 함수 목록으로만 다루지 않는다.

```text
Tool Definition
→ Registry 등록
→ 현재 세션에서 사용 가능 여부 판정
→ 필요한 Tool만 검색·노출
→ Permission 판정
→ 실행
→ Tool Result를 메시지에 추가
```

여행 Agent는 모든 외부 API Tool Schema를 매번 Prompt에 넣지 않고, 여행 목적과 현재 단계에 필요한 Tool만 활성화하는 구조를 학습한다.

### 5. 질문 Tool과 구조화된 출구

원본 13장의 `AskUserQuestion`과 `SyntheticOutput`은 여행 Agent에 직접 대응된다.

질문은 자연어 문장만 반환하지 않는다.

```json
{
  "type": "ask_user",
  "questions": [
    {
      "field": "budget",
      "question": "1인당 예산은 어느 정도인가요?",
      "options": ["50만원 이하", "50~100만원", "100만원 이상"]
    }
  ],
  "maxQuestions": 1
}
```

최종 일정도 화면에 표시할 구조화된 `ItineraryOutput`으로 만들고, React용 표현과 LLM 내부 결과를 분리한다.

### 6. Sub-agent 격리와 결과 요약

원본 14~15장의 핵심은 Agent 숫자가 아니라 Context 격리다.

```text
부모 State
→ 필요한 입력만 추출
→ Sub-agent 전용 Runtime
→ 전문 작업
→ 구조화 결과와 요약만 부모에게 반환
```

Research Agent가 전체 대화와 다른 Agent의 내부 추론을 모두 받지 않게 한다. Sub-agent 결과는 부모 Context에 그대로 붙이지 않고 `CandidatePlaces`, `SourceSummary`, `Confidence`로 정리한다.

### 7. TaskType와 Coordinator

원본 16~17장은 실행 중인 작업을 Task로 모델링하고 Coordinator가 작업을 조정한다. 여행 Agent에서는 `ASK_REQUIREMENTS`, `RESEARCH_PLACES`, `BUILD_ITINERARY`, `VALIDATE_ROUTE`, `VALIDATE_BUDGET`, `WAIT_APPROVAL`처럼 작업 타입을 정의한다.

Coordinator는 LLM이 아니다. 실행 순서, 병렬 가능 여부, 재시도, Checkpoint, 종료 조건을 관리하는 런타임 컴포넌트다.

### 8. MCP와 Permission의 결합

원본 18~19장의 중요한 점은 MCP 연결과 권한을 따로 생각하지 않는 것이다.

```text
MCP Tool 발견
→ Tool Schema 검증
→ 세션·사용자 권한 판정
→ 읽기/쓰기 작업 분리
→ 필요하면 사용자 승인
→ 실행 결과와 승인 결과를 Event로 발행
```

날씨 조회는 자동 실행할 수 있지만, 캘린더 등록·예약·결제는 `WAIT_APPROVAL` 상태를 거쳐야 한다.

### 9. Hooks와 확장 지점

원본 20~22장의 Hooks, Skill/Plugin/Output Style, Feature Flag도 축소해 반영한다.

여행 Agent의 Hook 후보는 `before_tool_call`, `after_tool_call`, `before_compaction`, `after_compaction`, `before_external_write`, `on_error`다.

여행 스타일은 Prompt를 코드에 하드코딩하지 않고 Output Style 또는 Profile로 확장한다.

### 10. Retry, Fallback, 취소

원본 05장과 23장은 네트워크 오류를 한 가지 catch로 끝내지 않는다.

```text
timeout / overload
→ 제한된 재시도
→ backoff
→ fallback model 또는 대체 Tool
→ 사용자에게 복구 불가 상태 전달
```

같은 Tool을 무한 재시도하지 않고, `retryable`, `user_action_required`, `permanent_failure`를 구분한다.

## 여행 Agent에 적용할 최종 요청 흐름

```text
사용자: 여행 일정 추천해줘
    ↓
QueryEngine이 Session과 State 로드
    ↓
ContextManager가 Prompt 구성
    ↓
LLM이 AgentDecision 생성
    ↓
ASK_USER: 목적지·날짜·예산·인원·취향 질문
    ↓
사용자 답변을 State에 병합
    ↓
충분성 판정
    ├─ 부족: 다시 ASK_USER
    └─ 충분: Coordinator 시작
            ↓
      필요한 Tool만 Registry에서 검색
            ↓
      Research / Route / Budget Sub-agent 실행
            ↓
      일정 생성 및 검증
            ↓
      필요 시 수정 Loop
            ↓
      ItineraryOutput과 근거 반환
```

## 학습 완료 기준

다음 질문에 코드와 문서로 답할 수 있어야 한다.

- QueryEngine과 QueryRuntime을 왜 나누는가?
- LLM 메시지에서 고정 영역과 동적 영역은 무엇인가?
- Tool Schema를 항상 노출하지 않는 이유는 무엇인가?
- Sub-agent에 전체 대화를 주지 않는 이유는 무엇인가?
- Compaction 후 어떤 정보를 반드시 복구해야 하는가?
- 읽기 Tool과 쓰기 Tool의 실행 정책은 어떻게 다른가?
- Coordinator와 Sub-agent 중 누가 실행 순서를 결정하는가?
- 네트워크 실패와 사용자 승인 대기를 어떻게 구분하는가?
