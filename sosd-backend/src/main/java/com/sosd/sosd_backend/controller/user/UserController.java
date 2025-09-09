package com.sosd.sosd_backend.controller.user;

import com.sosd.sosd_backend.dto.user.RecentRepoResponse;
import com.sosd.sosd_backend.service.user.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final GithubService githubService;

    @GetMapping("/recent-repos/{githubId}")
    public ResponseEntity<Map<String, Object>> getRecentRepos(@PathVariable Long githubId) {
        List<RecentRepoResponse> repos = githubService.getRecentRepos(githubId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "");
        response.put("data", Map.of("recentRepos", repos));

        return ResponseEntity.ok(response);
    }
}
