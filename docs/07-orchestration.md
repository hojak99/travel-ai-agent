# 07. Orchestration

Orchestrator는 Agent와 Tool의 실행 순서, 분기, 재시도, 종료를 관리한다. 전체 작업의 상태 머신이라고 보면 된다.

```text
조건 추출 → 정보 부족이면 질문 → 장소 검색 → 일정 생성
→ 이동 검증 → 예산 검증 → 오류가 있으면 일정 수정 → 최종 응답
```

날짜 검증과 예산 검증처럼 결정적인 단계는 LLM이 아니라 코드가 제어한다.

## Task와 Workflow를 구분하기

- `Workflow`: 여행 계획 전체의 상태 기계와 다음 단계 결정
- `Task`: 검색·경로 검증처럼 실행·취소·재시도 가능한 작업 한 개
- `AgentDecision`: 현재 턴에서 모델이 제안한 다음 행동

Coordinator는 이 세 개를 섞지 않는다. LLM은 후보 행동을 제안할 수 있지만 상태 전이 가능 여부, 병렬 실행, 재시도, 종료는 코드가 결정한다.

```text
COLLECTING_REQUIREMENTS
→ RESEARCHING
→ BUILDING_ITINERARY
→ VALIDATING
→ WAITING_APPROVAL | REVISING | COMPLETED | FAILED | CANCELLED
```

각 전이는 사전 조건, 생성 Event, 저장할 Checkpoint, 보상 또는 복구 동작을 가진다.

## 직접 구현 과제

- `TravelWorkflow`와 단계 enum을 만든다.
- 단계별 입력·출력 계약을 작성한다.
- timeout과 최대 재시도 횟수를 둔다.
- 중간 상태를 저장하고 실패 지점부터 재개한다.
- 정상, 정보 부족, Tool 실패, 검증 실패를 테스트한다.
- 같은 Checkpoint를 두 번 재개해도 중복 쓰기가 발생하지 않게 한다.
- 부모 취소를 병렬 Task에 전파하고 늦게 도착한 결과를 폐기한다.

## 학습 실험과 완료 기준

- Route와 Budget 검증을 병렬화했을 때 순차 실행과 결과·지연을 비교한다.
- 프로세스를 각 단계 직후 강제 종료하고 Checkpoint부터 재개한다.
- 동일 Event의 중복 전달과 순서 역전을 주입한다.
- 모든 상태에 명시적 종료·재개 경로가 있고 무한 Loop가 없으면 완료다.
