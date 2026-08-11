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
