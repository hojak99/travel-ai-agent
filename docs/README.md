# Travel AI Agent Study

Claude Code Source Study의 핵심 Agent 런타임 설계 패턴을 Spring Boot + React 여행 Agent에 적용해보는 학습 문서다.

## 목표

사용자가 "여행 일정 추천해줘"라고 입력하면 Agent가 부족한 조건을 자연스럽게 질문하고, 충분한 State가 쌓인 뒤 Coordinator가 필요한 Sub-agent, Local Tool, MCP Tool을 실행한다. 긴 대화는 Compaction으로 관리하고 React에는 실행 이벤트를 Streaming한다.

## 읽는 순서

```text
00 아키텍처 계약
01 QueryEngine과 첫 대화 흐름
02 State와 실행 Loop
03 System Prompt와 Output Style
04 Compaction 패밀리
05 Tool Protocol과 Tool Registry
06 Sub-agent와 Context 격리
07 Task와 Coordinator
08 MCP와 Permission
09 Memory와 Persistence
10 Streaming, Hook, Human-in-the-loop
11 LLM 메시지 구성과 Prompt Cache
12 프로젝트 설명
13 원본 34장 대응표
14 검토 체크리스트
15 원본 한국어판 대조 결과와 보강 계획
```

각 문서를 읽은 뒤 직접 구현한다. 문서 하나와 작은 구현 하나를 대응시키고, 구현하지 않은 기능은 구현한 것처럼 설명하지 않는다.

## 원본과의 관계

원본 `Claude-Code-Source-Study/docs-kr`를 번역해 복제한 문서가 아니다. 원본의 런타임 생명주기와 설계 패턴을 여행 도메인으로 옮긴 학습 문서다.

핵심 대응은 다음과 같다.

```text
QueryEngine Facade → QueryEngine
query Kernel       → QueryRuntime
query State        → TravelState
Tool Registry      → 여행 Tool Registry
Agent Runner       → 여행 Sub-agent Runtime
Coordinator        → TravelCoordinator
Compaction         → 대화·Tool 결과 압축과 State 복구
Permission         → 예약·결제·캘린더 승인
Store/Event        → React와 Agent Runtime 사이의 상태·이벤트 연결
```

## 구현 순서

1. ChatController와 QueryEngine Facade
2. QueryRuntime과 AgentDecision
3. StateStore와 여행 조건 추출
4. ContextManager와 PromptBuilder
5. ASK_USER Loop
6. Tool Definition, Registry, Permission
7. Local Tool과 Tool Result
8. Sub-agent Runtime과 결과 요약
9. TaskType, Coordinator, Checkpoint
10. Microcompact와 Full Compact
11. MCP와 외부 연동
12. Retry, fallback, 취소, Hook
13. React 상태 연결과 SSE

## 설계 원칙

- LLM은 판단과 자연어 생성을 담당한다.
- 계산, 검색, 저장, 검증은 코드나 Tool이 담당한다.
- Sub-agent는 전문성·Context 격리·병렬성이 필요할 때 사용한다.
- 대화 원문, 구조화된 여행 State, 장기 선호, 실행 Checkpoint를 분리한다.
- 외부 API와 쓰기 작업에는 timeout, retry, permission을 둔다.
- 모든 LLM 실행은 사용한 State와 Prompt를 추적할 수 있어야 한다.
