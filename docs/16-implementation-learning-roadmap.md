# 16. 구현·학습 로드맵

## 목적

이 프로젝트의 목표는 기능 수를 늘리는 것이 아니라 Agent 런타임의 설계 결정을 직접 구현하고 비교하며 설명할 수 있게 되는 것이다. 계층을 모두 만든 뒤 연결하지 않고, 매 단계마다 사용자 요청에서 응답까지 동작하는 **수직 슬라이스**를 완성한다.

## 단계별 학습 기록 형식

각 단계의 PR 또는 커밋에는 다음을 남긴다.

```text
관찰: Claude Code Source Study에서 본 패턴
가설: 이 패턴이 여행 Agent에서 해결할 문제
적용: 이번에 구현한 가장 작은 설계
실험: 정상·실패·적대적 시나리오
측정: 정확도, 지연, 토큰·비용, 복구 여부
결론: 유지·수정·제거 결정
```

“Claude Code가 이렇게 했기 때문”은 설계 근거가 아니다. 여행 도메인에서 해결할 문제가 없으면 이식하지 않는다.

## Phase 0. 기준선 만들기

한 번의 요청을 받아 LLM 응답을 반환하는 단순한 구현을 보존한다. 이후 구조화 Runtime과 비교할 baseline이다.

완료 기준:

- 고정 시나리오 10개와 기대 결과를 만든다.
- 모델, Prompt 버전, 지연, 토큰 사용량을 기록한다.
- API key나 개인정보가 Trace에 남지 않는다.

## Phase 1. 세션과 상태 기반 Loop

`QueryEngine`, `QueryRuntime`, `ConversationState`, `RuntimeStatus`를 연결한다. 부족한 조건을 질문하고 같은 세션의 다음 입력에서 이어 간다.

완료 기준:

- 서로 다른 세션이 격리된다.
- 동시 요청이 State를 유실하지 않는다.
- 최대 반복, 취소, 오류 종료가 있다.
- `GET /api/chat/{sessionId}/state`로 학습 중인 State를 관찰할 수 있다.

## Phase 2. 구조화 Decision과 Requirement Extraction

LLM 자연어와 제어 신호를 분리한다. `ASK_USER`, `CALL_TOOL`, `DELEGATE`, `FINAL`을 schema로 검증하고 사용자 답변을 `TravelState`에 병합한다.

완료 기준:

- 필수 필드별 extraction fixture가 있다.
- 모순되는 날짜·예산은 자동 확정하지 않고 재질문한다.
- 파싱 실패와 hallucinated action이 실행되지 않는다.
- 단순 추출·질문과 복잡한 일정 생성에 같은 모델·추론 예산을 쓸지 실험하고 `ModelPolicy` 결정을 기록한다.

## Phase 3. Tool과 Permission

결정적 계산·검증 하나를 Local Tool로 옮긴다. 그다음 읽기 Tool과 모의 쓰기 Tool을 추가해 권한 경계를 학습한다.

완료 기준:

- schema, timeout, 취소, 오류 분류가 있다.
- 읽기 병렬/쓰기 직렬 정책을 검증한다.
- 승인 payload와 실제 실행 인자가 일치한다.
- 중복 요청에도 쓰기가 한 번만 일어난다.

## Phase 4. Sub-agent와 Coordinator

먼저 단일 Agent로 end-to-end 일정을 만든 뒤, Context 격리나 병렬성의 이익이 측정되는 작업만 Sub-agent로 분리한다.

권장 순서:

1. `ResearchAgent`: 출처가 있는 후보 장소 반환
2. `ValidationAgent`: 이동·예산·영업시간을 적대적으로 검증
3. 필요할 때만 `RouteAgent`, `BudgetAgent` 분리

완료 기준:

- 단일 Agent baseline보다 분리 이유를 수치나 실패 사례로 설명한다.
- 자식 Context, Tool 권한, 예산, 종료 조건이 제한된다.
- 일부 실패와 늦은 결과를 Coordinator가 안전하게 처리한다.

## Phase 5. Persistence와 Compaction

State와 Event를 영속화하고 서버 재시작 후 실행을 재개한다. 그다음 실제로 Context 한계가 관찰될 때 Microcompact와 Full Compact를 추가한다.

완료 기준:

- 재시작·중복 Event·동시 갱신 테스트를 통과한다.
- 압축 전후 핵심 제약과 승인 상태가 같다.
- 연속 압축 실패가 무한 Loop를 만들지 않는다.

## Phase 6. Streaming과 MCP

SSE Event replay를 먼저 완성하고 읽기 전용 MCP Tool 하나를 연결한다. MCP를 붙이기 전과 후의 Runtime 계약은 같아야 한다.

완료 기준:

- 재연결 후 Event 누락과 중복이 없다.
- MCP 인증·연결 실패가 세션 전체 실패로 번지지 않는다.
- MCP Tool도 동일한 권한·검증·Trace 경계를 통과한다.

## Phase 7. Hardening

[17 평가와 보안](./17-evaluation-and-security.md)의 회귀 세트와 위협 시나리오를 CI에 넣는다. 이 단계 전에는 실제 예약·결제를 연결하지 않는다.

## 단계 공통 Definition of Done

- 실행 가능한 API 또는 UI 데모가 있다.
- 정상 경로뿐 아니라 timeout, 취소, 잘못된 schema, 중복 요청 테스트가 있다.
- 최소 Trace로 Decision과 상태 전이를 재생할 수 있다.
- 새 추상화가 해결한 구체적 문제와 제거 조건을 문서화한다.
- 다음 단계 기능을 미리 일반화하지 않는다.
