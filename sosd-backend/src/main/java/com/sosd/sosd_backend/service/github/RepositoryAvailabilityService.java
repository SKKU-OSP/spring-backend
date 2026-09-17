package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.github_collector.api.GithubGraphQLClient;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DB에 이미 등록된 저장소가 현재 공개 토큰으로 조회 가능한지 재확인한다.
 *
 * <p>GitHub는 삭제된 저장소와 권한 없는 비공개 저장소에 모두 NOT_FOUND를
 * 반환할 수 있으므로 원인을 단정하지 않는다. 명시적인 NOT_FOUND가 여러 수집
 * 주기에 걸쳐 반복된 경우에만 PUBLICLY_UNAVAILABLE로 확정한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryAvailabilityService {

    private static final int BATCH_SIZE = 20;

    private final GithubGraphQLClient graphQLClient;
    private final GithubRepositoryRepository repositoryRepository;

    @Value("${github.collector.repository-unavailable-threshold:3}")
    private int confirmationThreshold;

    @Transactional
    public void refreshAvailability(List<RepoRef> repoRefs) {
        if (repoRefs == null || repoRefs.isEmpty()) return;

        Map<Long, GithubRepositoryEntity> entities = repositoryRepository
                .findAllByGithubRepoIdIn(repoRefs.stream().map(RepoRef::githubRepoId).toList())
                .stream()
                .collect(Collectors.toMap(GithubRepositoryEntity::getGithubRepoId, Function.identity()));

        for (int start = 0; start < repoRefs.size(); start += BATCH_SIZE) {
            List<RepoRef> batch = repoRefs.subList(start, Math.min(start + BATCH_SIZE, repoRefs.size()));
            try {
                refreshBatch(batch, entities, LocalDateTime.now());
            } catch (RuntimeException e) {
                // 네트워크/인증/서버 오류는 저장소 상태로 누적하지 않는다.
                log.warn("Repository availability check failed for batch; keeping previous states. size={}",
                        batch.size(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshBatch(List<RepoRef> batch,
                              Map<Long, GithubRepositoryEntity> entities,
                              LocalDateTime checkedAt) {
        QueryRequest request = buildQuery(batch);
        GithubGraphQLClient.GraphQLResponse<Map> response = graphQLClient.query(request.query())
                .variables(request.variables())
                .executeWithAutoRotate(Map.class);

        Map<String, Object> data = response.getData() == null ? Map.of() : response.getData();
        Map<String, GithubGraphQLClient.GraphQLError> errorsByAlias = errorsByAlias(response.getErrors());

        for (int i = 0; i < batch.size(); i++) {
            RepoRef ref = batch.get(i);
            String alias = "repo_" + i;
            GithubRepositoryEntity entity = entities.get(ref.githubRepoId());
            if (entity == null) continue;

            Object raw = data.get(alias);
            if (raw instanceof Map<?, ?> rawRepository) {
                Map<String, Object> repository = (Map<String, Object>) rawRepository;
                Long returnedId = longValue(repository.get("databaseId"));
                if (!ref.githubRepoId().equals(returnedId)) {
                    // 예전 owner/name을 다른 저장소가 재사용한 경우 잘못 연결하지 않는다.
                    entity.markPubliclyUnavailable(checkedAt, confirmationThreshold,
                            "저장소 경로가 다른 GitHub repository id를 가리킵니다.");
                    log.warn("Repository identity mismatch. expectedId={}, returnedId={}, path={}",
                            ref.githubRepoId(), returnedId, ref.fullName());
                    continue;
                }

                String nameWithOwner = stringValue(repository.get("nameWithOwner"));
                String[] canonicalName = splitNameWithOwner(nameWithOwner, ref.ownerName(), ref.repoName());
                entity.markAvailable(canonicalName[0], canonicalName[1],
                        booleanValue(repository.get("isPrivate")), checkedAt);
                if (!ref.fullName().equalsIgnoreCase(nameWithOwner)) {
                    log.info("Repository canonical name updated. id={}, {} -> {}",
                            ref.githubRepoId(), ref.fullName(), nameWithOwner);
                }
                continue;
            }

            GithubGraphQLClient.GraphQLError error = errorsByAlias.get(alias);
            if (isNotFound(error)) {
                entity.markPubliclyUnavailable(checkedAt, confirmationThreshold,
                        error == null ? "GitHub NOT_FOUND" : error.getMessage());
                log.info("Repository unavailable observation. id={}, path={}, failures={}, status={}",
                        ref.githubRepoId(), ref.fullName(), entity.getConsecutiveUnavailableCount(),
                        entity.getAvailabilityStatus());
            } else {
                // data null이어도 NOT_FOUND 근거가 없으면 일시 오류일 수 있으므로 유지한다.
                log.warn("Repository availability was inconclusive; keeping previous state. id={}, path={}, error={}",
                        ref.githubRepoId(), ref.fullName(), error == null ? "none" : error.getMessage());
            }
        }
    }

    private QueryRequest buildQuery(List<RepoRef> batch) {
        List<String> declarations = new ArrayList<>();
        List<String> selections = new ArrayList<>();
        Map<String, Object> variables = new LinkedHashMap<>();

        for (int i = 0; i < batch.size(); i++) {
            RepoRef ref = batch.get(i);
            declarations.add("$owner" + i + ": String!");
            declarations.add("$name" + i + ": String!");
            selections.add("repo_" + i + ": repository(owner: $owner" + i
                    + ", name: $name" + i + ", followRenames: true) { databaseId nameWithOwner isPrivate }");
            variables.put("owner" + i, ref.ownerName());
            variables.put("name" + i, ref.repoName());
        }

        String query = "query RepositoryAvailability(" + String.join(", ", declarations) + ") {\n"
                + String.join("\n", selections) + "\n}";
        return new QueryRequest(query, variables);
    }

    private Map<String, GithubGraphQLClient.GraphQLError> errorsByAlias(
            List<GithubGraphQLClient.GraphQLError> errors) {
        Map<String, GithubGraphQLClient.GraphQLError> result = new HashMap<>();
        if (errors == null) return result;
        for (GithubGraphQLClient.GraphQLError error : errors) {
            if (error.getPath() != null && !error.getPath().isEmpty()) {
                result.put(error.getPath().get(0), error);
            }
        }
        return result;
    }

    private boolean isNotFound(GithubGraphQLClient.GraphQLError error) {
        if (error == null) return false;
        if ("NOT_FOUND".equalsIgnoreCase(error.getType())) return true;
        if (error.getExtensions() != null) {
            for (String key : List.of("type", "code")) {
                Object value = error.getExtensions().get(key);
                if (value != null && "NOT_FOUND".equalsIgnoreCase(value.toString())) return true;
            }
        }
        String message = error.getMessage() == null ? "" : error.getMessage().toLowerCase(Locale.ROOT);
        return message.contains("could not resolve to a repository");
    }

    private Long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private Boolean booleanValue(Object value) {
        return value instanceof Boolean bool ? bool : null;
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private String[] splitNameWithOwner(String value, String fallbackOwner, String fallbackRepo) {
        int separator = value.indexOf('/');
        if (separator <= 0 || separator == value.length() - 1) {
            return new String[]{fallbackOwner, fallbackRepo};
        }
        return new String[]{value.substring(0, separator), value.substring(separator + 1)};
    }

    private record QueryRequest(String query, Map<String, Object> variables) {}
}
