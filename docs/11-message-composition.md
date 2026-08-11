# 11. LLM 메시지 구성과 순서

## 목표

LLM에 매번 전체 데이터를 무작정 보내지 않고, 안정적인 규칙으로 Context를 구성한다.

## 기본 구성

```text
1. System Prompt
2. Runtime 규칙과 현재 실행 정보
3. 구조화된 Travel State
4. Compacted Summary
5. 최근 대화 메시지
6. Tool 정의와 호출 규칙
7. 현재 사용자 요청
```

실제 SDK가 요구하는 순서는 사용하는 모델 API의 계약을 따르되, 애플리케이션 내부에서는 이 논리적 영역을 분리해 관리한다.

## Tool Loop 메시지

Tool을 호출할 때는 다음 흐름을 보존한다.

```text
user message
→ assistant tool call
→ tool result
→ assistant next decision
```

Tool 결과를 일반 사용자 메시지처럼 위조하거나, assistant의 Tool 호출 없이 Tool 결과만 추가하면 대화 의미가 깨질 수 있다.

## ASK_USER 메시지

Agent가 질문할 때는 자연어만 반환하지 않고 내부 Decision도 만든다.

```json
{
  "action": "ASK_USER",
  "question": "여행 예산은 어느 정도인가요?",
  "missingFields": ["budget"]
}
```

사용자 답변이 들어오면 State를 갱신한 뒤 새 Loop를 시작한다.

## 직접 구현 과제

- `ContextAssembler`를 만들고 각 영역을 별도 메서드로 구성한다.
- `AgentDecision` schema를 정의한다.
- 메시지 순서와 Tool Loop를 테스트한다.
- Compaction 후에도 확정 일정과 제약 조건이 Prompt에 포함되는지 검증한다.
