# 08. MCP

MCP는 Agent가 외부 도구와 데이터에 일정한 방식으로 연결되도록 하는 프로토콜이다. Local Tool이 애플리케이션 내부 함수라면 MCP Tool은 외부 서버가 제공한다.

```text
Travel Agent → MCP Client → Weather / Map / Calendar MCP Server
```

MCP를 도입해도 Tool schema, 권한, timeout, 오류 처리는 애플리케이션의 책임이다.

## 직접 구현 과제

- Local Tool과 MCP Tool을 같은 `Tool` 추상화로 호출한다.
- 외부 서버 연결 실패를 명시적인 오류 결과로 변환한다.
- 읽기 작업과 예약·등록 작업의 권한을 분리한다.
- 사용자 승인 없이 캘린더를 변경하지 않는다.
