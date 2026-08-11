# 12. 이 프로젝트를 설명하는 방법

## 핵심 설명

이 프로젝트는 Claude Code의 구체적인 구현을 복사한 것이 아니라, 실제 Agent 제품에서 사용하는 런타임 설계 패턴을 여행 도메인에 적용한 학습 프로젝트다.

사용자가 여행 계획을 요청하면 Agent는 곧바로 일정을 생성하지 않는다. QueryRuntime이 현재 State를 읽고, 필요한 정보가 없으면 `ASK_USER` Decision으로 추가 질문을 한다. 충분한 조건이 모이면 Coordinator가 Research, Budget, Route, Validation Sub-agent를 실행한다.

각 Sub-agent는 전체 대화가 아니라 작업에 필요한 Context만 받는다. 계산·검색·검증은 Local Tool 또는 MCP Tool이 수행하고, 예약이나 캘린더 등록처럼 부작용이 있는 작업은 Permission Manager가 사용자 승인을 요구한다.

대화가 길어지면 CompactionManager가 오래된 메시지를 요약하지만, 목적지·기간·예산·취향·확정 일정 같은 구조화 State는 별도로 보존한다. React에는 최종 답변뿐 아니라 Agent 단계와 Tool 실행 이벤트도 Streaming한다.

## Claude Code 개념과의 대응

| 학습 개념 | 여행 Agent 구현 |
|---|---|
| QueryEngine | 대화 요청의 진입점과 실행 Facade |
| Query Runtime | 한 번의 여행 계획 실행 Context |
| Tool Registry | 여행 Local Tool과 MCP Tool 목록 |
| Agent Definition | Research/Budget/Route Agent의 역할·권한·출력 계약 |
| Coordinator | Sub-agent와 Tool의 실행 조정 |
| Task/Checkpoint | 진행 중인 여행 계획 작업과 재개 위치 |
| Compaction | 오래된 대화를 요약하고 여행 제약 보존 |
| Permission | 예약, 결제, 캘린더 등록 승인 |
| Event/Streaming | React에 실행 단계와 결과 전달 |

## 학습 완료 기준

누군가에게 다음을 코드와 함께 설명할 수 있으면 이 프로젝트의 1차 목표를 달성한 것이다.

- 왜 모든 요청을 단일 Agent가 처리하지 않는가?
- 언제 Sub-agent를 사용하고 언제 사용하지 않는가?
- Compaction에서 무엇을 요약하고 무엇을 구조화 State로 보존하는가?
- Tool 호출 전후의 LLM 메시지 순서는 어떻게 되는가?
- Local Tool과 MCP Tool은 어떤 경계로 나누는가?
- 사용자의 추가 답변이 들어오면 어느 State와 Loop가 갱신되는가?
