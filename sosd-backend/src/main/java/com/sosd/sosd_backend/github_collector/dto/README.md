# DTO 패키지

이 디렉토리는 **`github_collector` 모듈에서만 사용하는 DTO(Data Transfer Object) 클래스**를 관리합니다.

## 규칙
- 외부 API(GitHub) 요청/응답 매핑 객체
- `github_collector` 내부 로직에서만 사용하는 데이터 전달 객체
- 서비스 전역에서 재사용될 가능성이 있는 **Entity** 클래스는 `src/main/java/com/sosd/sosd_backend/entity/github`에 위치
