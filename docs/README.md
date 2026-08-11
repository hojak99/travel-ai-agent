# Travel AI Agent Study

Claude Code Source Study의 핵심 개념을 Spring Boot + React 여행 Agent에 적용해보는 학습 문서다.

## 읽는 순서

```text
01 전체 구조
02 State와 대화 루프
03 System Prompt
04 Compaction
05 Tool 시스템
06 Sub-agent
07 Orchestration
08 MCP
09 Memory와 Persistence
10 Streaming과 Human-in-the-loop
```

각 문서를 읽은 뒤 `구현 과제`를 직접 코드로 작성한다. 한 번에 모든 기능을 만들지 않고, 문서 하나와 작은 구현 하나를 대응시킨다.

## 원칙

- LLM은 판단과 자연어 생성을 담당한다.
- 계산, 검색, 저장, 검증은 코드나 Tool이 담당한다.
- Agent 간 통신은 자유 형식 텍스트보다 구조화된 상태를 사용한다.
- 외부 API는 timeout, retry, 실패 응답을 항상 설계한다.
- 대화 기록과 여행 계획 상태를 구분한다.

## 구현 순서

1. Controller와 단일 Agent
2. State와 대화 저장
3. 실제 LLM Adapter
4. 여행 조건 추출
5. Local Tool
6. Sub-agent와 Coordinator
7. Compaction과 Persistence
8. MCP와 외부 연동
9. React 채팅 UI와 SSE
