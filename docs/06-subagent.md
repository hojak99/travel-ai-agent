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

## 직접 구현 과제

- 각 Agent의 입력과 출력 record를 정의한다.
- `CandidatePlace`, `BudgetSummary`, `ItineraryValidation`을 반환한다.
- 각 Agent가 State 일부만 받게 한다.
- 한 Agent 실패 시 fallback을 설계한다.
