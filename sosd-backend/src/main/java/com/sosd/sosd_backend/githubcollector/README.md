# GitHub Collector 구조

이 모듈은 GitHub API로부터 사용자 깃허브 기여내역 데이터를 수집하고 저장하는 기능을 계층적으로 분리하여 구현합니다. 유지보수성과 확장성을 고려한 구조로 설계되어 있습니다.

---

## 📁 디렉토리 구조 예시 (repository 예시)

```
githubcollector/
├── api/
│   └── BaseGitHubClient.java         # GitHub API 호출 공통 로직 (예: 인증, 헤더 설정 등) 정의하는 추상 클래스
│   └── RepoGitHubClient.java         # 단일 페이지 GitHub API 요청 담당 (BaseGitHubClient 상속 및 구현)
│
├── collector/
│   └── RepoCollector.java            # 페이지네이션 처리 포함 전체 저장소 수집
│
├── service/
│   └── RepoService.java              # 비즈니스 로직 처리: 중복 제거, 데이터 가공, DB 저장까지 담당
│
├── repository/
│   └── UserRepository.java           # 사용자 정보를 DB에서 조회
│   └── RepoRepository.java           # 수집된 저장소 데이터를 JPA로 DB에 저장
│
├── scheduler/
│   └── GitHubScheduler.java          # 주기적으로 실행되어 전체 수집 로직 트리거 (예: @Scheduled 사용)
│
├── dto/
│   └── RepoDto.java                  # GitHub API 응답을 내부 도메인에 맞게 매핑하는 DTO 클래스
│
└── entity/
    └── RepoEntity.java               # DB 테이블(repo)과 매핑되는 JPA Entity 클래스
...
```

---

## Repository 수집 데이터 흐름 예시

1. `GitHubScheduler`가 주기적으로 실행되며, 수집 작업을 시작합니다.
2. `UserRepository`를 통해 모든 사용자 정보를 조회합니다.
3. 조회된 각 사용자에 대해 `RepoService`가 수집 프로세스를 담당합니다.
4. `RepoService`는 `RepoCollector`를 호출하여 GitHub 저장소 정보를 요청합니다.
5. `RepoCollector`는 내부적으로 `RepoClient`를 통해 GitHub API를 호출합니다.
6. API에서 받아온 응답은 DTO 객체(`RepoListDto`)로 매핑되어 `RepoCollector`로 반환됩니다.
7. 수집된 결과는 `RepoService`에서 가공된 후 `RepoRepository`를 통해 JPA로 데이터베이스에 저장됩니다.


---
