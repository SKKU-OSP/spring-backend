package com.sosd.sosd_backend.ai_evaluation.util;

public class ReadmeScoreCalculator {

    public static double calculateTotal(
            int clarity,
            int readability,
            int reproducibilityCode,
            int reproducibilityResult,
            int visual,
            int license,
            int collaboration
    ) {
        int core = clarity
                + readability
                + Math.min(reproducibilityCode + reproducibilityResult, 4); // 재현성 최대 4점
        int bonus = visual + license + collaboration;
        return Math.min(core + bonus, 15);
    }

    // 13~15 → A+, 9~12 → A, 6~8 → B, 3~5 → C, 0~2 → D
    public static String toGrade(double total) {
        if (total > 12) return "A+";
        if (total >= 9)  return "A";
        if (total >= 6)  return "B";
        if (total >= 3)  return "C";
        return "D";
    }
}