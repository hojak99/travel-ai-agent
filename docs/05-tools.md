# 05. Tool 시스템

Tool은 LLM이 호출할 수 있는 명확한 함수다. 입력 schema, 권한, timeout, 오류 결과를 별도로 설계한다.

원본 10장의 흐름을 여행 Agent에 옮기면 `정의 → 등록 → 현재 세션에서 활성화 → 권한 판정 → 실행 → 결과 기록`이 된다. Registry에 존재하는 Tool 전체를 매번 모델에 노출하지 않는다.

## 여행 Local Tool

```text
calculateBudget
calculateTravelTime
validateOpeningHours
saveItinerary
exportCalendar
```

LLM이 비용을 직접 계산하지 않고 `calculateBudget`를 호출하게 한다. Tool은 자연어보다 구조화된 결과를 반환한다.

```json
{"ok": true, "total": 420000, "currency": "KRW"}
```

## 실행 계약

모든 Local/MCP Tool은 최소한 다음 메타데이터를 가진다.

```text
name, description, inputSchema
risk: READ_ONLY | WRITE | FINANCIAL
parallelSafe, timeout, retryPolicy
idempotencyRequired, resultSchema
```

Tool 실패를 예외 문자열 하나로 합치지 않는다.

```text
SUCCESS
RETRYABLE_FAILURE
USER_ACTION_REQUIRED
PERMANENT_FAILURE
CANCELLED
```

읽기 전용이면서 `parallelSafe`인 호출만 병렬 실행한다. 쓰기 호출은 승인과 멱등성 키를 확인한 후 직렬화한다.

## Tool 결과의 안전성

외부 Tool 결과는 Prompt의 지시가 아니라 데이터다. HTML·Markdown·MCP 텍스트에 포함된 지시문을 분리하고, 크기 제한과 schema 검증을 거친 뒤 Context에 넣는다. 로그에는 API key, 결제 토큰, 원문 개인정보를 남기지 않는다.

## 직접 구현 과제

- `ToolDefinition`, `ToolInput`, `ToolResult`를 정의한다.
- Tool Registry에서 이름으로 Tool을 찾는다.
- 입력 검증과 timeout을 추가한다.
- 성공, 재시도 가능 오류, 영구 오류를 구분한다.
- 현재 Workflow 단계에 필요한 Tool만 선택하는 `ToolSelector`를 만든다.
- 승인된 인자와 실제 실행 인자가 같은지 검사한다.
- 쓰기 Tool에 idempotency key를 적용한다.

## 학습 실험과 완료 기준

- 읽기 Tool 두 개가 병렬 실행되고 쓰기 Tool은 직렬화되는지 검증한다.
- timeout, 429, 잘못된 schema, 사용자 취소를 각각 주입한다.
- Tool 설명을 Prompt에서 제거했을 때와 선택적으로 노출했을 때 정확도·토큰 사용량을 비교한다.
- 모델이 존재하지 않는 Tool이나 승인되지 않은 인자를 요청해도 실행되지 않으면 완료다.
