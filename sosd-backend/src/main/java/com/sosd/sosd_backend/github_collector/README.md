# GitHub Data Collector Architecture

## 개요

GitHub API를 통해 사용자별 기여 데이터(커밋, PR, 이슈, 스타)를 효율적으로 수집하는 시스템입니다. **증분 수집**을 통해 중복 수집을 방지하고 속도를 향상시켰으며, 인터페이스 기반 설계로 확장성을 보장합니다.

## 핵심 특징

- **증분 수집**: SHA 기반(커밋) / 날짜 기반(PR, 이슈, 스타) 증분 처리
- **사용자 중심 수집**: 등록된 사용자의 기여만 선별적 수집
- **인터페이스 기반 설계**: 새로운 수집기 추가 용이
- **스케줄링 지원**: 백그라운드 자동 수집

## 증분 수집 전략

### 커밋 (SHA 기반)
- **이유**: 브랜치 머지로 인한 날짜 순서 불일치 문제 해결
- **방식**: HEAD부터 last_processed_sha까지 역순 수집
- **장점**: 실제 main 브랜치(default branch) 반영 순서 보장

### PR/이슈/스타 (날짜 기반)
- **이유**: 생성 시점이 곧 반영 시점
- **방식**: since 파라미터로 last_processed_date 이후 데이터 수집
- **장점**: 단순하고 효율적

## 커서 관리

```sql
-- 증분 수집 상태 저장
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


## 시스템 구조

### 전체 아키텍처

```
백그라운드 스케줄러 (복수 유저에 대한 요청)
    ↓
CollectByUsers(userList)
    ↓
CollectBySingleUser(user)  <- Controller단의 API호출(단일 유저에 대한 호출)
    ↓
각종 Collector 구현체들 (CommitCollector, PRCollector, IssueCollector, StarCollector)
```


## 주요 컴포넌트
**!!아래 코드는 모두 의사코드이며, 구현 중 세부사항이 변경될 수 있습니다**

### 1. 스케줄러 레이어

```java
@Scheduled(fixedRate = 3600000) // 1시간마다 실행
public void scheduleDataCollection() {
    List<User> users = userService.getAllActiveUsers();
    collectByUsers(users);
}

public void collectByUsers(List<User> userList) {
    for (User user : userList) {
        try {
            collectBySingleUser(user);
        } catch (Exception e) {
            log.error("Failed to collect data for user: {}", user.getGithubId(), e);
        }
    }
}
```

### 2. 사용자별 수집 오케스트레이터

```java
public void collectBySingleUser(User user) {
    // 1. 레포 목록 수집 및 갱신
    List<Repository> repoList = repoCollector.collectRepositories(user);
    
    // 2. 각 레포별 기여 데이터 수집
    for (Repository repo : repoList) {
        for (CollectorInterface collector : collectorList) { // collector에 대한 구현체 (CommitCollector, PrCollector.. 등등)
            try {
                // API 호출과 DB 저장 분리
                List<?> collectedData = collector.collect(user, repo);
                if (!collectedData.isEmpty()) {
                    collector.persist(user, repo, collectedData);
                }
            } catch (Exception e) {
                log.error("Failed to collect {} for user: {} repo: {}", 
                    collector.getType(), user.getGithubId(), repo.getFullName(), e);
            }
        }
    }
}
```

### 3. 수집기 인터페이스

```java
public interface CollectorInterface<T> {
    List<T> collect(User user, Repository repo);  // 외부 API 호출
    void persist(User user, Repository repo, List<T> data);  // DB 저장
    String getType(); // "commit", "pr", "issue", "star"
}
```

### 4. 커밋 수집기 (SHA 기반 증분)

- commit은 예전에 작성했어도 merge가 늦게 되면 날짜순이 꼬일 수 있기 때문에 **날짜 기준 증분 수집이 불가능함**
- default branch 기준으로 마지막으로 수집한 sha를 DB에 저장해두고, **최근 sha부터 조상 sha로 거슬로 올라가서 마지막 수집 sha를 만날 때까지 수집**

```java
@Component
public class CommitCollector implements CollectorInterface<Commit> {
    
    @Override
    public List<Commit> collect(User user, Repository repo) {
        // 1. 마지막 처리된 SHA 조회
        String lastProcessedSha = cursorService.getLastSha(user.getGithubId(), repo.getId(), "commit");
        
        // 2. GitHub API 호출 (author 필터링)
        List<Commit> commitList = githubApiClient.getCommits(repo.getFullName(), user.getGithubLoginUsername());
        
        // 3. 증분 처리 - 새로운 커밋만 필터링
        List<Commit> newCommits = new ArrayList<>();
        for (Commit commit : commitList) {
            if (commit.getSha().equals(lastProcessedSha)) {
                break; // 이전에 처리된 지점까지 도달
            }
            newCommits.add(commit);
        }
        
        // 4. 시간순으로 정렬 (오래된 것부터)
        Collections.reverse(newCommits);
        return newCommits;
    }
    
    @Override
    public void persist(User user, Repository repo, List<Commit> commits) {
        // 1. 트랜잭션 내에서 배치 저장
        commitService.saveCommitsBatch(commits, user.getGithubId(), repo.getId());
        
        // 2. 커서 업데이트 (가장 최신 SHA로)
        if (!commits.isEmpty()) {
            String latestSha = commits.get(commits.size() - 1).getSha(); // 마지막이 최신
            cursorService.updateCursor(user.getGithubId(), repo.getId(), "commit", latestSha, null);
        }
    }
    
    @Override
    public String getType() {
        return "commit";
    }
}
```

### 5. PR/이슈/스타 수집기 (날짜 기반 증분)

- commit과 달리 날짜순으로 수집해도 중간에 추가되는 요소가 없기 때문에 마지막 수집 날짜 기준으로 증분 수집

```java
@Component
public class PRCollector implements CollectorInterface<PullRequest> {
    
    @Override
    public List<PullRequest> collect(User user, Repository repo) {
        // 1. 마지막 처리된 날짜 조회
        LocalDateTime lastProcessedDate = cursorService.getLastProcessedDate(
            user.getGithubId(), repo.getId(), "pr");
        
        // 2. GitHub API 호출 (since 파라미터 사용)
        List<PullRequest> prList = githubApiClient.getPullRequests(
            repo.getFullName(), 
            user.getGithubLoginUsername(),
            lastProcessedDate
        );
        
        return prList;
    }
    
    @Override
    public void persist(User user, Repository repo, List<PullRequest> pullRequests) {
        // 1. 배치 저장
        pullRequestService.savePullRequestsBatch(pullRequests, user.getGithubId(), repo.getId());
        
        // 2. 가장 최신 날짜 계산
        LocalDateTime latestDate = pullRequests.stream()
            .map(PullRequest::getCreatedAt)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        // 3. 커서 업데이트
        if (latestDate != null) {
            cursorService.updateCursor(user.getGithubId(), repo.getId(), "pr", null, latestDate);
        }
    }
    
    @Override
    public String getType() {
        return "pr";
    }
}

@Component
public class IssueCollector implements CollectorInterface<Issue> {
    
    @Override
    public List<Issue> collect(User user, Repository repo) {
        LocalDateTime lastProcessedDate = cursorService.getLastProcessedDate(
            user.getGithubId(), repo.getId(), "issue");
        
        return githubApiClient.getIssues(
            repo.getFullName(), 
            user.getGithubLoginUsername(),
            lastProcessedDate
        );
    }
    
    @Override
    public void persist(User user, Repository repo, List<Issue> issues) {
        issueService.saveIssuesBatch(issues, user.getGithubId(), repo.getId());
        
        LocalDateTime latestDate = issues.stream()
            .map(Issue::getCreatedAt)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        if (latestDate != null) {
            cursorService.updateCursor(user.getGithubId(), repo.getId(), "issue", null, latestDate);
        }
    }
    
    @Override
    public String getType() {
        return "issue";
    }
}

@Component 
public class StarCollector implements CollectorInterface<Star> {
    
    @Override
    public List<Star> collect(User user, Repository repo) {
        LocalDateTime lastProcessedDate = cursorService.getLastProcessedDate(
            user.getGithubId(), repo.getId(), "star");
        
        return githubApiClient.getStars(
            repo.getFullName(),
            user.getGithubLoginUsername(), 
            lastProcessedDate
        );
    }
    
    @Override
    public void persist(User user, Repository repo, List<Star> stars) {
        starService.saveStarsBatch(stars, user.getGithubId(), repo.getId());
        
        LocalDateTime latestDate = stars.stream()
            .map(Star::getStarredAt)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        if (latestDate != null) {
            cursorService.updateCursor(user.getGithubId(), repo.getId(), "star", null, latestDate);
        }
    }
    
    @Override
    public String getType() {
        return "star";
    }
}
```