package com.sosd.sosd_backend.service;

import com.sosd.sosd_backend.entity.github.GithubScoreEntity;

public interface ScoreCalculator {
    GithubScoreEntity calculateScore(Long githubId, String studentId, Integer year);
}