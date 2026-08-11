# 09. Memory와 Persistence

## 저장할 것을 구분하기

```text
대화 Memory: 사용자가 말한 원문
여행 State: 현재 계획의 구조화된 값
장기 Preference: 반복 사용자의 취향
Checkpoint: Workflow 실행 위치
```

이 네 가지를 하나의 거대한 대화 테이블에 넣지 않는다.

## 직접 구현 과제

- `Conversation`, `TravelPlan`, `UserPreference`, `WorkflowCheckpoint` 경계를 정한다.
- 서버 재시작 후에도 sessionId로 대화를 이어간다.
- 동시 요청에서 State 유실을 막을 버전 필드를 검토한다.
- 개인정보와 외부 API 응답의 보존 기간을 정한다.
