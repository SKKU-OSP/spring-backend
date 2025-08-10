package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.RepositoryDetailDto;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepoUpsertService {

    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GithubAccountRepository githubAccountRepository;

    /**
     * 주어진 GitHub 계정이 기여한 저장소 목록을 DB에 Upsert(Insert or Update) 처리합니다.
     * <ul>
     *   <li>PK(repoId) 기준 신규 저장소는 INSERT, 기존 저장소는 UPDATE 수행</li>
     *   <li>호출 전 외부 API 수집은 완료되어 있어야 함</li>
     *   <li>트랜잭션 전체를 커버하여 원자성 보장</li>
     * </ul>
     *
     * @param githubLoginUsername GitHub 계정 로그인 ID
     * @param dtos 수집된 저장소 상세 정보 리스트
     * @throws RuntimeException 해당 계정이 DB에 존재하지 않을 경우
     */
    @Transactional
    public void upsertRepos(String githubLoginUsername, List<RepositoryDetailDto> dtos){
        // 2-1) 계정 로딩
        GithubAccount githubAccount = githubAccountRepository.findByGithubLoginUsername(githubLoginUsername)
                .orElseThrow(() -> new RuntimeException("Github Account 없음: " + githubLoginUsername));

        // 2-2) 레포지토리 엔티티로 변환
        List<GithubRepositoryEntity> entities = dtos.stream()
                .map(dto -> toEntity(dto, githubAccount))
                .toList();

        // 2-3) 저장 (한 계정이 보유한 repository는 보통 그렇게 많지 않기 때문에 네이티브 사용x)
        githubRepositoryRepository.saveAll(entities);

    }

    private GithubRepositoryEntity toEntity(RepositoryDetailDto dto, GithubAccount account){
        return GithubRepositoryEntity.builder()
                .repoId(dto.id())
                .ownerName(dto.ownerNameOnly())
                .repoName(dto.repoNameOnly())
                .defaultBranch(nvl(dto.defaultBranch(), "main"))
                .githubRepositoryCreatedAt(toUtcLocal(dto.createdAt()))
                .githubRepositoryUpdatedAt(toUtcLocal(dto.updatedAt()))
                .pushedAt(toUtcLocal(dto.pushedAt()))
                .githubAccount(account)
                .build();
    }

    private static LocalDateTime toUtcLocal(OffsetDateTime odt) {
        return odt != null ? odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    private static String nvl(String v, String def) {
        return (v == null || v.isEmpty()) ? def : v;
    }

}
