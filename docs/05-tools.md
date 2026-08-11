# 05. Tool 시스템

Tool은 LLM이 호출할 수 있는 명확한 함수다. 입력 schema, 권한, timeout, 오류 결과를 별도로 설계한다.

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

## 직접 구현 과제

- `ToolDefinition`, `ToolInput`, `ToolResult`를 정의한다.
- Tool Registry에서 이름으로 Tool을 찾는다.
- 입력 검증과 timeout을 추가한다.
- 성공, 재시도 가능 오류, 영구 오류를 구분한다.
