# 00. 프로젝트 아키텍처 계약

## 프로젝트 목표

이 프로젝트는 자연스러운 여행 계획 대화를 제공하면서, Claude Code에서 볼 수 있는 Agent 런타임 구조를 여행 도메인에 맞게 직접 구현하고 학습하는 것을 목표로 한다.

Claude Code의 소스 코드를 복제하는 것이 아니다. Query, State, Tool, Agent, Coordinator, Permission, Memory, Compaction, Event의 설계 경계를 참고해 같은 종류의 문제를 해결한다.

## 한 문장 설명

> 이 여행 Agent는 QueryEngine 중심의 상태 기반 Agent 런타임으로, 대화 중 부족한 조건을 질문하고, 충분한 State가 모이면 Coordinator가 Sub-agent와 Local/MCP Tool을 실행하며, 긴 대화는 Compaction과 Persistence로 관리한다.

## 목표 런타임 구조

```text
React Chat UI
    ↓
ChatController
    ↓
QueryEngine
    ↓
QueryRuntime
    ├─ ContextManager
    ├─ RequirementCollector
    ├─ PromptBuilder
    ├─ AgentRunner
    ├─ ToolRegistry
    ├─ MCPClient
    ├─ PermissionManager
    ├─ CompactionManager
    ├─ EventPublisher
    └─ StateStore
```

## 한 번의 요청 생명주기

```text
요청 수신
→ Session/State 로드
→ Context 구성
→ LLM Decision 생성
→ ASK_USER / CALL_TOOL / DELEGATE / FINAL 중 하나 실행
→ State와 Event 저장
→ 종료 또는 다음 Loop
```

## 책임 경계

- QueryEngine: 요청 단위 실행을 시작하고 결과를 반환한다.
- QueryRuntime: 한 번의 실행에서 사용하는 State, Context, Loop를 관리한다.
- RequirementCollector: 빠진 여행 조건을 찾아 질문한다.
- Coordinator: 여러 Agent와 Tool의 순서를 조정한다.
- ToolRegistry: 사용 가능한 Tool을 등록하고 이름으로 찾는다.
- AgentRunner: 격리된 Context로 Sub-agent를 실행한다.
- CompactionManager: 오래된 대화를 요약하고 핵심 State를 보존한다.
- PermissionManager: 예약·결제·캘린더 등록 전 승인을 처리한다.
- EventPublisher: Agent 단계와 Tool 실행 상태를 React에 전달한다.

## 설계 원칙

1. Agent는 부족한 정보를 질문할 수 있어야 한다.
2. 결정적인 계산과 검증은 LLM이 아니라 코드나 Tool이 담당한다.
3. Sub-agent는 전문성, Context 격리, 병렬성이 필요한 경우에만 사용한다.
4. 대화 원문, 구조화된 여행 State, 장기 선호, 실행 Checkpoint를 분리한다.
5. 외부 API와 쓰기 작업에는 timeout, retry, permission을 둔다.
6. 모든 LLM 실행은 어떤 State와 Prompt를 사용했는지 추적 가능해야 한다.
7. LLM의 Decision은 제안이며, schema 검증·권한 판정·도메인 불변식 검사를 통과해야 실행된다.
8. 외부 검색 결과와 MCP 응답은 신뢰하지 않는 데이터로 취급하고 지시문과 사실 데이터를 분리한다.
9. 동일 요청의 재시도는 예약·결제·저장 작업을 중복 실행하지 않아야 한다.

## 런타임 불변식

- 하나의 `sessionId`에는 한 시점에 하나의 쓰기 전이만 적용한다.
- 사용자 메시지, LLM Decision, Tool 호출, Tool 결과는 추적 가능한 순서를 가진다.
- `FINAL`은 필수 여행 조건과 검증 결과가 충족된 경우에만 가능하다.
- `WAIT_APPROVAL` 이후 실행하는 인자는 사용자가 승인한 인자와 같아야 한다.
- 취소된 Runtime은 새로운 Tool 호출이나 외부 쓰기를 시작하지 않는다.
- Compaction 이후에도 구조화된 여행 조건, 미해결 질문, 승인 상태, 출처가 복구된다.

## 신뢰 경계

```text
신뢰하지 않음: 사용자 입력, 웹 문서, MCP/외부 API 결과, LLM 출력
조건부 신뢰: 검증된 ToolResult, 저장소에서 읽은 세션 데이터
코드가 강제: schema, 권한, 예산·날짜 불변식, 멱등성, 보존 정책
```

Prompt에 적은 금지 문구만으로 보안을 구현하지 않는다. `PermissionManager`, 입력 검증기, URL 정책, 저장소 제약처럼 모델 밖의 코드 경계에서 강제한다.

## 범위와 비범위

1차 학습 범위는 대화형 여행 계획 생성과 읽기 전용 정보 조회다. 실제 결제, 자동 예약, 운영용 다중 테넌트, 고가용성 분산 실행은 설계 계약과 모의 Tool까지만 구현한다. 이 경계를 넘길 때는 [17 평가와 보안](./17-evaluation-and-security.md)의 출시 관문을 먼저 통과한다.
