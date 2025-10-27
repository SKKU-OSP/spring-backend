package com.sosd.sosd_backend.controller.user;

import com.sosd.sosd_backend.dto.user.DashboardContributionResponse;
import com.sosd.sosd_backend.service.user.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/dashboard")
@RequiredArgsConstructor
public class UserDashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/{githubId}/contr")
    public ResponseEntity<Map<String, Object>> getUserContribution(@PathVariable Long githubId) {
        DashboardContributionResponse data = dashboardService.getUserContribution(githubId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
