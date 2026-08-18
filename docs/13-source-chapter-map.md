# 13. 원본 34장과 여행 Agent 학습 문서 대응표

이 프로젝트의 docs는 원본 `Claude-Code-Source-Study/docs-en`을 그대로 번역한 문서가 아니다. 원본의 런타임 생명주기와 설계 패턴을 여행 Agent에 맞게 재구성한 학습 문서다.

## 반드시 반영할 원본 축

| 원본 장 | 원본 핵심 | 우리 문서 | 여행 Agent 적용 |
|---:|---|---|---|
| 01 | 전체 구조와 진입점 | 00, 01 | React API, Agent Runtime, MCP 진입점 |
| 05 | QueryEngine Facade와 query Kernel | 01, 02, 16 | 세션 단위 Facade, 한 요청의 실행 Loop, 단계별 복구 |
| 06 | System Prompt의 섹션 조립 | 03, 11, 17 | 여행 규칙·State·Tool 설명의 안정적 조립과 Injection 평가 |
| 07 | 다단계 Compaction과 복구 | 04 | Microcompact, Full Compact, State 재구성 |
| 08 | Prompt Cache의 정적·동적 경계 | 03, 11 | 고정 Prompt와 세션별 Context 분리 |
| 10 | Tool Protocol, Registry, ToolSearch | 05, 17 | 등록·검색·권한 확인·실행 분리와 적대적 Tool 입력 |
| 13 | 질문·스케줄·합성 Tool | 05, 07 | 추가 질문과 여행 Workflow 이벤트 |
| 14 | Agent 정의와 Sub-agent 호출 | 06 | 역할·Context·출력 계약이 있는 전문 Agent |
| 15 | 내장 Agent 설계 패턴 | 06 | Research, Route, Budget, Validation Agent |
| 16 | Task 모델과 TaskType 계보 | 07, 16 | 작업 상태와 실행 타입, 수직 슬라이스 복구를 구조화 |
| 17 | Coordinator와 예약 실행 | 07, 16 | Coordinator 중심 실행과 향후 일정 알림 |
| 18 | MCP Protocol 구현 | 08, 17 | 날씨·지도·캘린더 외부 Tool과 연결 위협 모델 |
| 19 | Permission과 승인 결과 전달 | 08, 10, 17 | 예약·결제·캘린더 등록 승인과 payload binding |
| 20 | Hooks | 10 | 실행 전·후·중단·오류 이벤트 확장점 |
| 21 | Skill·Plugin·Output Style | 12 | 여행 스타일과 도메인 확장점 |
| 23 | Transport와 API Retry | 02, 05, 08, 10, 17 | SSE, timeout, retry, fallback, 멱등성 |
| 31 | 다층 Memory | 09, 17 | 세션·여행 계획·사용자 선호·장기 기억과 보존 정책 |
| 33 | Store와 UI/비즈니스 상태 연결 | 09, 10, 17 | React 상태와 Agent Runtime 상태 연결·재생 |
| 34 | 이식 가능한 11가지 패턴 | 12, 14, 16, 17 | 설계 선택, 검토, 실험, 트레이드오프 설명 |

## 처음에는 보류할 원본 장

02~04의 CLI cold start, 기업 MDM, 설정 migration, 09의 Thinking/Effort, 11~12의 Shell·LSP, 22의 Feature Flag, 24~30의 Bridge·터미널 UI는 현재 여행 Agent의 핵심 학습 흐름 뒤로 미룬다.

다만 제품을 운영 단계로 확장할 때는 03·04·19·20·22·23을 다시 읽는다. 이 장들은 설정 변경, 권한, 확장, 네트워크 실패를 다룬다.

## 원본을 읽을 때 가져올 형식

각 원본 장에서 다음 다섯 가지를 추출해 우리 문서에 기록한다.

1. 어떤 문제를 해결하는가?
2. 런타임 책임은 어느 모듈에 있는가?
3. 상태와 이벤트는 어떤 순서로 이동하는가?
4. 실패·취소·재시도 시 어떤 복구 경로를 타는가?
5. 여행 Agent에서 어떤 구현 과제로 검증할 것인가?

기록할 때는 `관찰`, `적용`, `가설`을 구분한다. 분석 자료의 구현 세부를 영구한 제품 계약처럼 표현하지 않고, 우리 프로젝트에서 테스트한 결과만 확정된 설계로 승격한다.

## 중요한 구분

원본에 등장하는 `AsyncGenerator`, `Store`, `ToolUseContext`, `TaskType`, `bridgePointer` 같은 이름은 그대로 복사할 대상이 아니다. Spring Boot와 React에서 같은 책임을 갖는 컴포넌트로 대응시키되, 왜 이름과 경계를 다르게 선택했는지 문서에 남긴다.
