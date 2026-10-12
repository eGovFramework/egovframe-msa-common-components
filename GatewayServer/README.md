# GatewayServer

## 서비스 상태 (현재 모듈 기준)

| 항목 | 내용 |
|------|------|
| **역할** | Spring Cloud **Gateway** (WebFlux). 라우팅·CORS·JWT(`Auth` 필터) 처리 |
| **애플리케이션명** | `GatewayServer` |
| **포트** | `9000` (고정) — 브라우저·클라이언트의 **단일 진입점** |
| **Config Server** | `optional:configserver:http://localhost:8888`, 프로필 `local` |
| **Eureka** | 클라이언트로 등록·조회 (`lb://서비스명` 로드밸런싱) |
| **선행 조건** | EurekaServer, ConfigServer |

## 라우팅 개요

`src/main/resources/application.yml` 의 `spring.cloud.gateway.routes` 에 정의된다. 예시:

| 클라이언트 경로(접두) | 백엔드(Eureka) | 비고 |
|----------------------|----------------|------|
| `/main/**` | `lb://EGOVMAIN` | 메인 포털 |
| `/uat/uia/**` | `lb://EGOVLOGIN` | 로그인(URL 일부는 Auth 필터 제외) |
| `/uat/uap/**` | `lb://EGOVLOGINPOLICY` | 로그인정책 |
| `/sec/**` | `lb://EGOVAUTHOR` | 권한·롤·그룹 |
| `/cop/**` | `lb://EGOVBOARD` | 게시판·협업 |
| `/uss/olp/**` | `lb://EGOVQUESTIONNAIRE` | 설문 |
| `/sym/ccm/**` | `lb://EGOVCMMNCODE` | 공통코드 |
| `/ext/ops/**` | `lb://EGOVSEARCH` | 검색 UI·API |

보호가 필요한 경로에는 `filters: - name: Auth` 가 붙어 JWT 쿠키를 검사한다.

## 유의사항

- 본 모듈 `pom.xml` 기준 **Java 17**.
