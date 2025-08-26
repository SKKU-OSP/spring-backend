# GitHub Data Collector

## 개요

GitHub API를 통해 사용자별 기여 데이터(커밋, PR, 이슈, 스타)를 효율적으로 수집하는 시스템입니다. 계층별 설계와 증분 수집을 통해 안정적이고 확장 가능한 데이터 수집을 제공합니다.

## 핵심 특징

- **계층별 책임 분리**: 스케줄러 → 사용자 → GitHub 계정 → 레포지토리 → 리소스
- **증분 수집**: SHA 기반(커밋) / 날짜 기반(PR, 이슈, 스타)
- **독립적 호출**: 각 계층별로 개별 API 호출 지원
- **관심사 분리**: API 호출과 DB 저장 로직 분리

## 시스템 아키텍처

```
Scheduler (스케줄러)
    ↓
UserCollectionOrchestrator (사용자별 수집)
    ↓
GithubAccountCollector (GitHub 계정별 수집)
    ↓
RepositoryCollector (레포지토리별 수집)
    ↓
ResourceCollectors (리소스별 수집: Commit, PR, Issue, Star)
```

## 모듈별 상세 설명
**!! 아래 코드는 아키텍쳐 설명을 위한 의사 코드 예시이며, 구현 중 세부사항은 변경될 수 있습니다**

### 1. Scheduler - 최상위 스케줄러

전체 사용자에 대한 주기적 데이터 수집을 담당합니다.

```java
public class DataCollectionScheduler {
    
    public void scheduleDataCollection() {
        List<User> activeUsers = userService.getAllActiveUsers();
        
        for (User user : activeUsers) {
            userCollectionOrchestrator.collectByUser(user);
        }
    }
}
```

**역할:**
- 활성 사용자 목록 조회
- 각 사용자별 수집 작업 실행
- 스케줄링 관리 (예: 1시간마다 실행)

### 2. UserCollectionOrchestrator - 사용자별 수집 오케스트레이터

한 사용자에 연결된 모든 GitHub 계정의 데이터를 수집합니다.

```java
public class UserCollectionOrchestrator {
    
    public void collectByUser(User user) {
        // 1. 해당 사용자의 모든 GitHub 계정 조회
        List<GithubAccount> githubAccounts = githubAccountService.getAccountsByUser(user.getStudentId());
        
        // 2. 각 GitHub 계정별로 수집 실행
        for (GithubAccount githubAccount : githubAccounts) {
            githubAccountCollector.collectByGithubAccount(githubAccount);
        }
        
        // 3. 사용자별 수집 완료 시간 업데이트
        userService.updateLastCrawlingTime(user.getStudentId());
    }
}
```

**역할:**
- 사용자 → GitHub 계정 목록 매핑
- 하위 GitHub 계정 수집기 호출
- 사용자별 수집 상태 관리

### 3. GithubAccountCollector - GitHub 계정별 수집기

특정 GitHub 계정에 속한 모든 레포지토리의 데이터를 수집합니다.

```java
public class GithubAccountCollector {
    
    public void collectByGithubAccount(GithubAccount githubAccount) {
        // 1. 해당 GitHub 계정의 모든 레포 조회/갱신
        List<Repository> repositories = repositoryService.collectAndUpdateRepositories(githubAccount);
        
        // 2. 각 레포별로 리소스 수집 실행
        for (Repository repository : repositories) {
            repositoryCollector.collectByRepository(githubAccount, repository);
        }
        
        // 3. GitHub 계정별 수집 완료 시간 업데이트
        githubAccountService.updateLastCrawlingTime(githubAccount.getGithubId());
    }
}
```

**역할:**
- GitHub 계정 → 레포지토리 목록 매핑
- 레포지토리 메타데이터 수집/갱신
- 하위 레포지토리 수집기 호출
- 계정별 수집 상태 관리

### 4. RepositoryCollector - 레포지토리별 수집기

특정 레포지토리의 모든 리소스(커밋, PR, 이슈, 스타)를 수집합니다.

```java
public class RepositoryCollector {
    
    public void collectByRepository(GithubAccount githubAccount, Repository repository) {
        // 모든 리소스 수집기 실행 (Commit, PR, Issue, Star)
        for (ResourceCollectorInterface collector : resourceCollectors) {
            // API 호출과 DB 저장 분리
            List<?> collectedData = collector.collect(githubAccount, repository);
            
            if (!collectedData.isEmpty()) {
                collector.persist(githubAccount, repository, collectedData);
            }
        }
    }
}
```

**역할:**
- 레포지토리별 모든 리소스 타입 수집 조율
- 리소스 수집기들의 실행 관리
- 수집 결과 검증 및 저장 호출

### 5. ResourceCollectors - 리소스별 수집기들

#### 5.1 수집기 인터페이스

모든 리소스 수집기가 구현해야 하는 공통 인터페이스입니다.

```java
public interface ResourceCollectorInterface<T> {
    List<T> collect(GithubAccount githubAccount, Repository repository);  // API 호출
    void persist(GithubAccount githubAccount, Repository repository, List<T> data);  // DB 저장
    String getType();  // 리소스 타입 반환
}
```

#### 5.2 CommitCollector - 커밋 수집기

**SHA 기반 증분 수집**으로 커밋 데이터를 수집합니다.

```java
public class CommitCollector implements ResourceCollectorInterface<Commit> {
    
    public List<Commit> collect(GithubAccount githubAccount, Repository repository) {
        // SHA 기반 증분 수집
        String lastSha = cursorService.getLastSha(githubAccount.getGithubId(), repository.getId(), "commit");
        List<Commit> allCommits = githubApiClient.getCommits(repository.getFullName(), githubAccount.getGithubLoginUsername());
        
        // 새로운 커밋만 필터링 후 반환
        return filterNewCommits(allCommits, lastSha);
    }
    
    public void persist(GithubAccount githubAccount, Repository repository, List<Commit> commits) {
        commitService.saveCommitsBatch(commits, githubAccount.getGithubId(), repository.getId());
        
        // 조상 커밋을 거슬러 올라가면서 마지막 수집 지점까지 update
        if (!commits.isEmpty()) {
            String latestSha = commits.get(commits.size() - 1).getSha();
            cursorService.updateCursor(githubAccount.getGithubId(), repository.getId(), "commit", latestSha, null);
        }
    }
    
    public String getType() { return "commit"; }
}
```

**특징:**
- **증분 수집 방식**: SHA 기반 (마지막 수집한 커밋 SHA부터 최신까지)
- **이유**: 브랜치 머지로 인한 날짜 순서 불일치 문제 해결

#### 5.3 PullRequestCollector, IssueCollector, StarCollector

**날짜 기반 증분 수집**으로 데이터를 수집합니다.

```java
public class PullRequestCollector implements ResourceCollectorInterface<PullRequest> {
    
    public List<PullRequest> collect(GithubAccount githubAccount, Repository repository) {
        // 날짜 기반 증분 수집
        LocalDateTime lastDate = cursorService.getLastProcessedDate(githubAccount.getGithubId(), repository.getId(), "pr");
        return githubApiClient.getPullRequests(repository.getFullName(), githubAccount.getGithubLoginUsername(), lastDate);
    }
    
    public void persist(GithubAccount githubAccount, Repository repository, List<PullRequest> pullRequests) {
        pullRequestService.savePullRequestsBatch(pullRequests, githubAccount.getGithubId(), repository.getId());
        
        // Github API의 기간 쿼리를 이용하여 증분형 수집
        if (!pullRequests.isEmpty()) {
            LocalDateTime latestDate = pullRequests.stream()
                .map(PullRequest::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            cursorService.updateCursor(githubAccount.getGithubId(), repository.getId(), "pr", null, latestDate);
        }
    }
    
    public String getType() { return "pr"; }
}
```

**특징:**
- **증분 수집 방식**: 날짜 기반 (마지막 수집 날짜 이후)
- **적용 리소스**: PR, Issue, Star

### 6. CollectionController - API 엔드포인트 예시

각 계층별로 독립적인 수집 API를 제공합니다.

```java
public class CollectionController {
    
    // 사용자별 전체 수집
    public void collectUser(String studentId) {
        User user = userService.getByStudentId(studentId);
        userCollectionOrchestrator.collectByUser(user);
    }
    
    // GitHub 계정별 수집 
    public void collectGithubAccount(String githubUsername) {
        GithubAccount account = githubAccountService.getByUsername(githubUsername);
        githubAccountCollector.collectByGithubAccount(account);
    }
    
    // 특정 레포만 수집
    public void collectRepository(String owner, String repo) {
        Repository repository = repositoryService.getByFullName(owner + "/" + repo);
        GithubAccount account = githubAccountService.getByRepoOwner(owner);
        repositoryCollector.collectByRepository(account, repository);
    }
}
```

**제공 API 예시:**
- `/api/collect/user/{studentId}`: 특정 사용자의 모든 데이터 수집
- `/api/collect/github/{githubUsername}`: 특정 GitHub 계정의 모든 데이터 수집
- `/api/collect/repo/{owner}/{repo}`: 특정 레포지토리만 수집

## 증분 수집 전략

### 커서 관리

각 GitHub 계정-레포지토리-리소스 타입별로 마지막 수집 지점을 저장합니다.

```sql
CREATE TABLE github_sync_cursors (
    github_id BIGINT,
    github_repo_id BIGINT,
    resource_type ENUM('commit', 'issue', 'pr', 'star'),
    last_processed_sha VARCHAR(40),     -- 커밋용
    last_processed_at DATETIME,        -- PR/이슈/스타용
    last_updated_at DATETIME,
    PRIMARY KEY (github_id, github_repo_id, resource_type)
);
```
