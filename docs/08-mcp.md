# 08. MCP

MCP는 Agent가 외부 도구와 데이터에 일정한 방식으로 연결되도록 하는 프로토콜이다. Local Tool이 애플리케이션 내부 함수라면 MCP Tool은 외부 서버가 제공한다.

```text
Travel Agent → MCP Client → Weather / Map / Calendar MCP Server
```

MCP를 도입해도 Tool schema, 권한, timeout, 오류 처리는 애플리케이션의 책임이다.

## 연결 생명주기

```text
설정 로드 → 연결/인증 → capability·Tool 발견 → schema 검증
→ 이름 충돌 해결 → Registry proxy 등록 → health 관찰
→ 재연결 또는 graceful shutdown
```

연결 상태는 `DISCONNECTED`, `CONNECTING`, `CONNECTED`, `NEEDS_AUTH`, `FAILED`처럼 명시적으로 모델링한다. 서버가 보내는 Tool 이름과 schema를 그대로 신뢰하지 않고 namespace, 허용 목록, 최대 크기를 적용한다.

## MCP와 권한의 결합

MCP는 transport이지 신뢰 경계의 우회로가 아니다. 발견된 Tool도 Local Tool과 같은 위험 등급과 승인 정책을 통과한다. Calendar MCP의 조회와 일정 생성은 서로 다른 capability로 취급한다.

## 직접 구현 과제

- Local Tool과 MCP Tool을 같은 `Tool` 추상화로 호출한다.
- 외부 서버 연결 실패를 명시적인 오류 결과로 변환한다.
- 읽기 작업과 예약·등록 작업의 권한을 분리한다.
- 사용자 승인 없이 캘린더를 변경하지 않는다.
- 연결 timeout, 인증 만료, 재연결 backoff와 최대 횟수를 정의한다.
- 같은 이름의 Tool 충돌과 실행 중 연결 종료를 테스트한다.
- MCP 결과의 Prompt Injection 문자열을 데이터로 격리한다.

## 1차 구현 범위와 완료 기준

처음에는 MCP 서버 하나와 읽기 전용 Tool 하나만 연결한다. 다중 transport, OAuth 자동 갱신, 동적 서버 설치는 후속 단계로 미룬다. Local/MCP Tool이 Runtime에서 동일한 `ToolResult`로 관찰되고, 연결 실패가 전체 세션을 죽이지 않으면 완료다.
