package com.sosd.sosd_backend.service.user;


import com.sosd.sosd_backend.dto.user.DashboardContributionResponse;
import com.sosd.sosd_backend.repository.github.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboardRepository;

    public DashboardContributionResponse getUserContribution(Long githubId) {
        Object[] result = (Object[]) dashboardRepository.findDashboardContr(githubId);

        Long repoNum     = ((Number) result[0]).longValue();
        Long commits     = ((Number) result[1]).longValue();
        Long commitLines = ((Number) result[2]).longValue();
        Long issues      = ((Number) result[3]).longValue();
        Long prs         = ((Number) result[4]).longValue();

        return new DashboardContributionResponse(repoNum, commits, commitLines, issues, prs);
    }
}
