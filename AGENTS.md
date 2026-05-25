# AGENTS.md

## 프로젝트 개요
이 프로젝트는 공공데이터를 활용한 청년 맞춤형 서비스 "Y-kit" 입니다.
현재 프로젝트는 대부분 구현이 완료된 상태이며,
레거시 구조 개선, 성능 개선, 유지보수성 향상을 목표로 대규모 리팩토링을 진행 중입니다.

---
# 현재 리팩토링 목표
주요 목표:
- Service 책임 분리
- Query 최적화
- N+1 제거
- 응답 속도 개선
- 코드 가독성 향상
- 유지보수성 개선
- 병목 제거
- 중복 로직 제거

---
## 작업 전 필수 절차
구현 전에 반드시:
1. 현재 구조 분석
2. 관련 클래스 탐색
3. 기존 패턴 확인
4. 영향 범위 설명
5. 리팩토링 계획 먼저 제시

코드를 바로 작성하지 않는다.

---
## 기술 스택
- Java
- Spring Boot
- Spring Data JPA
- QueryDSL
- MySQL
- Docker

---
## 패키지 구조 규칙

### Controller
- request/response 처리만 담당
- 비즈니스 로직 작성 금지
- validation 및 요청 파라미터 처리만 수행

### Dto
- API 별 DTO 분리 유지
- request / response 패키지 분리
- DTO 재사용보다 명확성 우선
- Entity 직접 반환 금지

### Service
- 핵심 비즈니스 로직 담당
- transaction boundary 명확히 유지
- 역할 기반으로 책임 분리
- N+1 문제 항상 고려
- 과도하게 비대해지는 service는 분리 고려

CQRS 패턴 적용:
- FindService: 조회 전용
- CommandService: 생성/수정/삭제 전용
- 단, policy 패키지는 예외 가능

### Repository
- DB 접근만 담당
- 비즈니스 로직 작성 금지
- 복잡한 조회는 QueryDSL 우선 고려

---
## 코드 작성 규칙

### 함수 규칙
- 함수는 하나의 역할만 가져 책임 명확화한다.
- 함수 길이는 가능하면 짧게 유지한다.
- 중첩 depth 최소화
- 긴 조건문은 의미 단위로 분리

### 네이밍 규칙
- 현재 프로젝트 스타일 우선 유지
- 의미 명확한 이름 사용
- 축약어 사용 최소화
- boolean 변수는 is / has prefix 사용

### 에러 처리 규칙
- 모든 에러는 명확한 메시지 사용
- 예상 가능한 에러는 custom exception 사용
- try/catch 남용 금지
- 에러를 숨기거나 무시하지 않음
- RuntimeException 무분별 사용 금지

---
## 금지 사항
- 프로젝트 전체 구조 임의 변경 금지
- 사용하지 않는 abstraction 추가 금지
- 승인 없는 dependency 추가 금지
- 관련 없는 파일 수정 금지

- application-secrets.properties 접근 금지
- 외부 공공 API 호출 금지
- OpenAI API 호출 금지

특히 아래 서비스에서 AI 호출 실행 금지:
- PolicyAiAnalysisService
- PolicyComparisonService

테스트 또는 리팩토링 과정에서도 호출하지 않습니다.