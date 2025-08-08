# GitHub Collector 모듈 설계 문서

이 모듈은 GitHub API를 통해 사용자의 활동 데이터를 수집하고 저장하는 시스템으로, 유저 기반과 레포 기반 수집 방식을 유연하게 지원합니다. 증분 수집과 전체 수집 모두를 구조화하여 설계하였습니다.

---

## 📌 아키텍처 개요

```
github_collector/
├── api/                   # GitHub API 호출 (REST/GraphQL)
├── collector/             # 수집기 계층
│   ├── RepoCollector.java           # 유저 단위 레포 수집
│   ├── RepoDataCollector.java       # 공통 수집 인터페이스
│   └── impl/                        # 커밋/PR/이슈 등 구현체
│       ├── CommitCollector.java
│       ├── IssueCollector.java
│       └── PrCollector.java
│       └── StarCollector.java
│       └── ForkCollector.java
├── service/               # 비즈니스 로직 및 저장 처리
├── dto/                   # GitHub 응답 DTO
├── entity/                # DB Entity
└── scheduler/             # 주기적 수집 실행
```

---

## ✅ 수집 전략

| 항목              | 수집 단위 | 외부 식별자 기준                                                 | 인터페이스 사용 여부           |
| --------------- | ----- |-----------------------------------------------------------| --------------------- |
| Repository      | 유저 단위 | `githubAccount.githubLoginUsername`                       | ❌ 단독 클래스 사용           |
| Commit/PR/Issue | 레포 단위 | `githubRepository.ownerName`, `githubRepository.repoName` | ✅ `RepoDataCollector` |

---

## ✅ 인터페이스 설계

### 📁 `RepoDataCollector.java`

```java
public interface RepoDataCollector {
    void collect(RepoDto repo, CollectionRange range); // 증분 수집
    void collectAll(RepoDto repo);                     // 전체 수집
}
```

### 📁 `CommitCollector.java` (예시)

```java
@Component
public class CommitCollector implements RepoDataCollector {
    public void collect(RepoDto repo, CollectionRange range) {
        // API 호출 후 service.save()
    }

    public void collectAll(RepoDto repo) {
        collect(repo, CollectionRangeFactory.fullThisYear());
    }
}
```

---

## ✅ 유저 단위 레포 수집

### 📁 `RepoCollector.java`

```java
@Component
public class RepoCollector {
    public List<RepoDto> collectByUser(User user) {
        // GitHub 로그인 기준으로 API 호출
        // DB 저장 후 RepoDto 리스트 반환
    }
}
```

---

## ✅ 수집 흐름 예시

```java
for (User user : userRepository.findAll()) {
    List<RepoDto> repos = repoCollector.collectByUser(user);
    for (RepoDto repo : repos) {
        for (RepoDataCollector collector : repoDataCollectors) {
            collector.collect(repo, CollectionRangeFactory.fromLastCollected(...));
        }
    }
}
```

---

## ✅ 요약

* **유저 단위 수집**: `RepoCollector` 단독 클래스
* **레포 단위 수집**: `RepoDataCollector` 인터페이스로 통일
* **수집 범위**: `CollectionRange`로 외부에서 명시적으로 제어
* **유연성과 확장성** 중심의 OOP 설계
