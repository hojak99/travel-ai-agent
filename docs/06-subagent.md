# 06. Sub-agent

Sub-agent는 전체 대화 문맥을 모두 공유하지 않고 특정 책임을 맡는 전문 Agent다.

```text
TravelCoordinator
├─ ResearchAgent
├─ BudgetAgent
├─ RouteAgent
└─ ValidationAgent
```

처음에는 여러 프로세스를 띄우지 말고 Spring Bean 또는 서비스 클래스로 분리한다. 핵심은 책임과 Context 경계다.

Sub-agent의 수가 많다고 좋은 Agent가 되는 것은 아니다. 다음 중 하나가 명확할 때만 분리한다.

- 서로 다른 Tool 권한이 필요하다.
- 병렬 실행으로 실제 지연 시간을 줄일 수 있다.
- 긴 검색 Context를 부모 대화에서 격리해야 한다.
- 독립된 출력 계약으로 품질을 검증할 수 있다.

단순한 순차 계산이나 짧은 프롬프트 차이는 일반 서비스 또는 Tool로 유지한다.

## AgentDefinition 계약

```text
name / role / model policy
input schema / output schema
allowed tools / denied tools
token·시간 예산 / 최대 반복
종료 조건 / fallback
```

부모는 전체 대화를 넘기지 않고 `ResearchRequest`, `RouteRequest`처럼 필요한 State의 투영만 전달한다. 자식은 원문 전체가 아니라 근거가 있는 구조화 결과와 짧은 요약을 반환한다.

```text
ResearchResult = candidates + sources + observedAt + confidence + warnings
```

## 직접 구현 과제

- 각 Agent의 입력과 출력 record를 정의한다.
- `CandidatePlace`, `BudgetSummary`, `ItineraryValidation`을 반환한다.
- 각 Agent가 State 일부만 받게 한다.
- 한 Agent 실패 시 fallback을 설계한다.
- 자식 Agent가 다른 Agent를 무제한 생성하지 못하게 깊이 제한을 둔다.
- 부모 취소가 자식과 자식 Tool 호출까지 전파되게 한다.
- Agent별 실행 Trace와 비용을 기록한다.

## 학습 실험과 완료 기준

- 단일 Agent와 Research/Validation 분리 구조의 품질·지연·토큰을 같은 시나리오로 비교한다.
- ResearchAgent에 예산·개인정보를 주지 않아도 되는지 Context 최소화를 검토한다.
- 자식 실패·timeout·취소가 부모 상태를 손상시키지 않는지 테스트한다.
- Sub-agent를 제거해야 할 조건까지 설명할 수 있으면 완료다.
