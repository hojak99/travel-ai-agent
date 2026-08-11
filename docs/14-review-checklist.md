# 14. 문서와 구현 검토 체크리스트

각 단계의 구현이 끝날 때 아래 질문에 답할 수 있어야 한다.

## Query Runtime

- [ ] 외부 요청을 받는 Facade와 실제 실행 Kernel이 분리되어 있는가?
- [ ] 한 요청의 Loop 상태와 세션 상태를 구분하는가?
- [ ] `ASK_USER`, `CALL_TOOL`, `DELEGATE`, `FINAL`, `ERROR` 종료 경로가 있는가?
- [ ] 중간 이벤트와 최종 결과를 구분하는가?

## 메시지와 Prompt

- [ ] 고정 System Prompt와 동적 Runtime Context를 분리하는가?
- [ ] Travel State, Compacted Summary, 최근 대화의 우선순위가 명확한가?
- [ ] Tool call과 Tool result의 메시지 순서를 보존하는가?
- [ ] Sub-agent마다 별도의 Prompt와 최소 Context를 구성하는가?

## Context와 Compaction

- [ ] 토큰 사용량과 출력 예약 공간을 계산하는가?
- [ ] 가벼운 Tool 결과 정리와 전체 요약을 구분하는가?
- [ ] Compaction 실패 시 반복을 중단하는가?
- [ ] Compaction 후 State·최근 메시지·Tool 결과를 재구성하는가?

## Tool과 MCP

- [ ] Tool 등록, 검색, 권한 판정, 실행이 분리되어 있는가?
- [ ] 읽기 Tool은 병렬 실행할 수 있고 쓰기 Tool은 직렬화되는가?
- [ ] timeout, retry, 취소, 오류 결과가 정의되어 있는가?
- [ ] Local Tool과 MCP Tool을 같은 실행 계약으로 감쌀 수 있는가?

## Agent와 Task

- [ ] Sub-agent의 역할·입력·출력·권한·종료 조건이 정의되어 있는가?
- [ ] Coordinator가 모든 세부 작업을 직접 수행하지 않는가?
- [ ] Task 상태와 Checkpoint로 중단 후 재개할 수 있는가?
- [ ] Sub-agent 결과를 요약해 부모 Context에 반환하는가?

## 안전성과 운영

- [ ] 예약·결제·캘린더 등록 전 사용자 승인을 받는가?
- [ ] Hook으로 실행 전·후 정책을 확장할 수 있는가?
- [ ] API overload, context too long, truncated output에 복구 경로가 있는가?
- [ ] 어떤 State와 Prompt로 판단했는지 추적할 수 있는가?

## 문서 품질

- [ ] 문서가 한글로 설명되고 핵심 기술 용어는 원문 표기를 병기하는가?
- [ ] 원본 장 번호와 우리 문서의 대응 관계가 적혀 있는가?
- [ ] 설계 설명 뒤에 직접 구현 과제가 있는가?
- [ ] 구현하지 않은 기능을 구현한 것처럼 설명하지 않는가?
