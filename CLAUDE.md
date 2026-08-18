# Travel AI Agent 프로젝트 지침

## 프로젝트 목적

이 프로젝트는 단순한 Spring Boot 코드 연습이 아니다.

목표는 React 채팅 UI와 Spring Boot Agent Runtime을 이용해, 사용자가 자연스럽게 여행 계획을 대화로 완성하는 AI Agent를 만들면서 Agent 아키텍처를 이해하는 것이다.

사용자가 여행 계획을 요청하면 Agent는 부족한 조건을 질문하고, 충분한 정보가 모이면 Local Tool·MCP Tool·Sub-agent를 사용해 여행 일정을 조사·생성·검증한다.

## 핵심 아키텍처

Claude Code Source Study의 구조와 설계 패턴을 여행 도메인에 적용한다. Claude Code의 소스 코드를 복제하는 것이 아니라, 다음 책임 경계를 학습하고 구현한다.

```text
React Chat UI
    ↓
Chat API
    ↓
QueryEngine
    ↓
QueryRuntime
    ↓
TAO Loop
    ├─ Think: 현재 State를 보고 다음 행동 판단
    ├─ Act: 질문·Tool·Sub-agent·최종 응답 실행
    └─ Observe: 사용자 답변·Tool 결과를 수신하고 State 갱신
```

Runtime 주변에는 다음 컴포넌트가 협력한다.

```text
QueryEngine
├─ Session / State Store
├─ Context Manager
├─ Prompt Builder
├─ Tool Registry
├─ Agent Runner
├─ Coordinator
├─ Compaction Manager
├─ Permission Manager
├─ Checkpoint Store
└─ Event Publisher / Streaming
```

## 설계 우선순위

1. Agent가 자연스럽게 추가 질문을 하고 사용자의 답변을 다음 판단에 반영해야 한다.
2. LLM은 판단과 자연어 생성을 담당하고, 계산·검색·저장·검증은 코드나 Tool이 담당해야 한다.
3. 대화 원문, 구조화된 여행 State, 장기 사용자 선호, 실행 Checkpoint를 분리한다.
4. Sub-agent는 전문성·Context 격리·병렬성이 필요할 때 사용한다.
5. 긴 대화는 Compaction하되 목적지·기간·예산·동행자·취향·확정 일정·미해결 질문은 보존한다.
6. 외부 API와 부작용이 있는 작업에는 timeout, retry, permission, 취소 경로를 둔다.
7. React에는 최종 답변뿐 아니라 Agent 단계·Tool 실행·승인 대기·오류 이벤트도 전달한다.

## Claude Code 개념 대응

| Claude Code 개념      | 이 프로젝트의 대응                                   |
|-----------------------|------------------------------------------------------|
| QueryEngine Facade    | 여행 Session의 Agent 실행 진입점                     |
| query Kernel          | 한 번의 사용자 요청을 처리하는 QueryRuntime          |
| Query State           | ConversationState와 구조화된 여행 조건               |
| Tool Registry         | Local Tool과 MCP Tool의 등록·검색·실행 목록          |
| Agent Definition      | Research·Route·Budget·Validation Agent의 역할과 계약 |
| Agent Runner          | 격리된 Context에서 Sub-agent 실행                    |
| Coordinator           | Agent와 Tool의 순서·분기·재시도·종료 조정            |
| TaskType / Checkpoint | 여행 계획 작업 종류와 중단 후 재개 위치              |
| Compaction            | 대화·Tool 결과 압축과 핵심 State 복구                |
| Permission            | 예약·결제·캘린더 등록 전 사용자 승인                 |
| Hooks                 | Tool·Compaction·외부 쓰기·오류 생명주기 확장점       |
| Store / Event         | Agent Runtime과 React 사이의 상태·이벤트 연결        |

## 메시지 구성 원칙

LLM에 보내는 Context는 매번 무작정 전체 대화를 넣지 않는다. 다음 논리적 영역을 구분한다.

```text
System Prompt
→ Agent 규칙과 여행 도메인 규칙
→ Tool 사용 규칙과 출력 Decision Schema
→ 현재 구조화된 Travel State
→ Compacted Summary
→ 최근 대화
→ 현재 사용자 메시지
```

Tool 호출 시 메시지 의미를 보존한다.

```text
user message
→ assistant tool call
→ tool result
→ assistant next decision
```

## 구현 방식

각 문서의 개념을 읽은 뒤 작은 기능으로 직접 검증한다. 구현 과제는 코드 스타일 평가가 아니라 Agent 동작을 이해하기 위한 실험이다.

권장 순서:

```text
01 QueryEngine과 Session State
→ 02 State와 실행 Loop
→ 03 Prompt와 Decision
→ 04 Compaction
→ 05 Tool Protocol과 Registry
→ 06 Sub-agent와 Context 격리
→ 07 Task와 Coordinator
→ 08 MCP와 Permission
→ 09 Memory와 Persistence
→ 10 Streaming과 Human-in-the-loop
```

각 단계에서 반드시 확인할 것:

- Agent가 현재 State를 읽고 다음 행동을 선택하는가?
- 사용자 답변이 같은 여행 Session에 누적되는가?
- Tool과 Sub-agent 결과가 State와 다음 Prompt에 반영되는가?
- 긴 Context를 줄여도 중요한 여행 제약 조건이 유지되는가?
- 실패·취소·승인 대기 후 Agent가 올바른 지점에서 재개되는가?

## 코드 주석 규칙

- 핵심 클래스와 메서드에는 **무슨 역할을 맡고 왜 이 경계가 필요한지** 짧게 주석으로 남긴다.
- 특히 State 전이, Runtime 종료 조건, Tool 실행, 권한, 재시도·취소처럼 의도가 코드만으로 드러나지 않는 메서드는 1~3줄의 Javadoc을 작성한다.
- 코드가 어떻게 동작하는지 줄마다 반복 설명하지 말고, 설계 의도·불변식·주의할 부작용만 설명한다.
- 단순 getter, 명백한 위임, Spring 설정 boilerplate에는 불필요한 주석을 달지 않는다.
- 동작이나 책임이 바뀌면 관련 주석도 함께 수정한다. 코드와 맞지 않는 오래된 주석은 남기지 않는다.

```java
/**
 * 동일 Session의 한 번의 Agent 실행을 직렬화해 State 갱신 순서를 보존한다.
 */
public QueryResult submit(QueryCommand command) { ...}
```

## 코드 구조 규칙

- `@Component`, `@Service`, `@Repository` 등 Spring Bean 내부에 별도 class·record·enum을 정의하지 않는다.
- `RuntimeCancellationRegistry.CancellationSignal`처럼 Runtime 상태나 컴포넌트 간 계약을 나타내는 타입은 역할과 생명주기에 맞는 `domain` 패키지에 독립 파일로 분리한다.
- Spring Bean은 의존성 연결과 동작을 담당하고, 전달 데이터·상태·결과 타입은 Bean 구현에 종속되지 않게 유지한다.

## 문서 작성 규칙

- 설명은 한글로 작성한다.
- `Agent`, `Tool`, `System Prompt`, `State`, `Context`, `Compaction`, `MCP`, `Sub-agent`, `QueryEngine`, `TAO` 같은 핵심 용어는
  원문 표기를 유지한다.
- 문서는 문제 정의 → Agent 실행 흐름 → 상태·이벤트 변화 → 실패·복구 → 직접 구현 과제 순서로 작성한다.
- Spring 계층 분리나 클래스 네이밍 자체를 학습 목표로 삼지 않는다.
- 구현하지 않은 기능을 구현한 것처럼 문서에 쓰지 않는다.
- 여행 도메인 예시를 사용해 개념을 설명한다.
