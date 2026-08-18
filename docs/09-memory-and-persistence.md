# 09. Memory와 Persistence

## 저장할 것을 구분하기

```text
대화 Memory: 사용자가 말한 원문
여행 State: 현재 계획의 구조화된 값
장기 Preference: 반복 사용자의 취향
Checkpoint: Workflow 실행 위치
```

이 네 가지를 하나의 거대한 대화 테이블에 넣지 않는다.

추가로 세션 이력 재생과 장기 Memory를 구분한다. 이력 재생은 “무슨 Event가 일어났는가”를 복원하고, Memory는 “다음 세션에도 도움이 되는 무엇을 기억할 것인가”를 선택한다.

## 쓰기와 회상 정책

- 현재 Travel State에서 다시 계산할 수 있는 값은 장기 Memory에 저장하지 않는다.
- 장기 Preference는 사용자 명시 동의 또는 명확한 설정을 통해 저장한다.
- 건강, 여권, 결제 정보는 기본적으로 장기 Memory에서 제외한다.
- Memory에는 출처, 생성 시각, 만료 시각, 사용자 수정·삭제 경로를 둔다.
- 모든 Memory를 매번 넣지 않고 현재 요청과 관련된 항목만 제한된 개수로 회상한다.

## 일관성과 동시성

세션 State에는 version을 두고 optimistic locking 또는 session 단위 직렬화를 적용한다. Event와 State snapshot을 함께 사용한다면 어떤 것이 단일 진실 공급원인지 정하고, 부분 저장 후 장애가 났을 때의 복구 규칙을 문서화한다.

## 직접 구현 과제

- `Conversation`, `TravelPlan`, `UserPreference`, `WorkflowCheckpoint` 경계를 정한다.
- 서버 재시작 후에도 sessionId로 대화를 이어간다.
- 동시 요청에서 State 유실을 막을 버전 필드를 검토한다.
- 개인정보와 외부 API 응답의 보존 기간을 정한다.
- 로그·Trace·Memory별 redaction과 삭제 정책을 분리한다.
- Event replay로 같은 State를 복구하는 테스트를 작성한다.
- 동시 갱신, 중복 요청, 프로세스 재시작을 실패 주입으로 검증한다.

## 완료 기준

- “대화 이력, 현재 State, Checkpoint, 장기 Preference”의 소유자와 수명을 설명할 수 있다.
- 사용자가 자신의 Memory를 조회·수정·삭제할 수 있다.
- 재시작과 동시 요청 뒤에도 확정 일정과 승인 상태가 유실되지 않는다.
