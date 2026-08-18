# 10. Streaming과 Human-in-the-loop

긴 작업은 최종 답변만 기다리게 하지 말고 진행 상태를 보낸다.

```text
조건 분석 중 → 장소 검색 중 → 일정 생성 중 → 이동 검증 중
```

Spring Boot에서는 SSE를 우선 검토하고, React는 이벤트를 받아 메시지와 Agent 단계 상태를 갱신한다.

SSE는 진행 표시일 뿐 State 저장소가 아니다. 재연결 시 `Last-Event-ID` 또는 별도 cursor로 누락 Event를 재생하고, 클라이언트는 `eventId`로 중복을 제거한다.

```text
RUN_STARTED → STEP_STARTED → TOOL_STARTED → TOOL_COMPLETED
→ APPROVAL_REQUIRED → RUN_RESUMED → FINAL | FAILED | CANCELLED
```

Event에는 `eventId`, `sessionId`, `runId`, `sequence`, `type`, `timestamp`, `safePayload`를 둔다. 내부 Prompt, 비밀 값, 전체 Tool 원문은 그대로 전송하지 않는다.

## Hook 확장 지점

Hook은 Runtime의 핵심 상태 전이를 대신하지 않고, 관찰·정책·감사를 끼워 넣는 좁은 확장점이다.

```text
before_model_call / after_decision
before_tool_call / after_tool_call
before_compaction / after_compaction
before_external_write / on_error / on_run_end
```

Hook에는 timeout과 실패 정책을 둔다. 감사 Hook 실패는 본 작업을 계속할 수 있지만, `before_external_write` 정책 Hook 실패는 페일클로즈드로 실행을 막는다. Hook이 새로운 권한을 부여하거나 검증되지 않은 Tool 인자를 직접 바꾸게 하지 않는다.

## 승인 대상

- 예산을 크게 초과하는 일정 확정
- 예약 또는 결제
- 캘린더 등록
- 민감한 정보 사용

승인은 단순한 “예/아니오”가 아니다. 사용자에게 대상, 날짜, 금액, 통화, 취소 조건을 보여 주고 그 exact payload의 hash와 만료 시각을 저장한다. 실행 직전 실제 인자와 다시 비교한다.

## 직접 구현 과제

- `WorkflowEvent`에 단계, 상태, 메시지, timestamp를 둔다.
- SSE가 끊겼을 때 마지막 checkpoint부터 재개한다.
- 승인 대기 상태를 State에 저장한다.
- 승인, 거절, 수정 요청을 구분한다.
- 연결 해제는 Runtime 취소와 구분하고, 명시적 취소 API를 둔다.
- 승인 요청의 만료·중복 클릭·다른 세션의 응답을 거부한다.
- 느린 클라이언트의 배압과 Event 보존 한도를 정의한다.
- Hook 설정을 실행 시작 시 snapshot으로 고정하고 중간 변경의 영향을 차단한다.
- Hook 실패가 본 실행을 막을지 계속할지 Event별 정책을 정의한다.

## 학습 실험과 완료 기준

- SSE를 중간에 끊고 cursor부터 재연결해 Event 누락·중복이 없는지 확인한다.
- 승인 후 실행 직전에 가격을 바꿔 재승인이 요구되는지 검증한다.
- 취소와 동시에 끝난 Tool의 늦은 결과가 State를 갱신하지 못하게 한다.
- 정책 Hook timeout에서 외부 쓰기가 차단되는지 확인한다.
- UI가 서버 Event를 재생해 같은 화면 상태를 만들 수 있으면 완료다.
