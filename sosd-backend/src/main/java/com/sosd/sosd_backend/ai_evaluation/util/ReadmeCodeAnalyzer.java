package com.sosd.sosd_backend.ai_evaluation.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadmeCodeAnalyzer {

    private static final List<String> BADGE_DOMAINS = List.of(
            "img.shields.io", "badge.fury.io", "travis-ci.org", "travis-ci.com",
            "codecov.io", "coveralls.io", "circleci.com", "appveyor.com",
            "github.com/badges", "badgen.net", "flat.badgen.net",
            "hits.seeyoufarm.com", "ko-fi.com/img", "visitor-badge"
    );

    private static final Pattern IMG_PATTERN =
            Pattern.compile("!\\[.*?]\\(([^)]+)\\)");

    private static final Pattern CODE_BLOCK =
            Pattern.compile("```", Pattern.MULTILINE);

    private static final Pattern LIST_OR_TABLE =
            Pattern.compile("^([-*+] |\\d+\\. |\\|)", Pattern.MULTILINE);

    private static final Pattern VERSION =
            Pattern.compile(
                    "(Python|Node|Ruby|Java|Go|PHP|Kotlin|Swift|Rust|C\\+\\+|TypeScript)\\s*[>=v]?\\s*\\d" +
                    "|>= ?\\d+\\.\\d+|~=\\d|\\^\\d+\\.\\d+",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern DEPS =
            Pattern.compile(
                    "pip install|pip3 install|npm install|npm i\\b|yarn (add|install)|" +
                    "conda install|bundle install|go get|go mod|cargo (add|build)|" +
                    "apt(-get)? install|brew install|gradle|mvn install",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern RUN_CMD =
            Pattern.compile(
                    "python\\d? |npm (run|start|dev)|yarn (run|dev|start)|" +
                    "docker(-compose)? (run|up)|streamlit run|java -jar|" +
                    "go run|cargo run|make\\b|flask run|uvicorn|gunicorn|" +
                    "node |npx ",
                    Pattern.CASE_INSENSITIVE);

    public record SubItemDetail(List<String> good, List<String> bad) {}

    /** 가독성 세부 항목별 충족/불충족 */
    public static SubItemDetail readabilityDetail(String readme) {
        List<String> good = new ArrayList<>();
        List<String> bad = new ArrayList<>();

        boolean hasH2 = Pattern.compile("^## [^#]", Pattern.MULTILINE).matcher(readme).find();
        boolean hasH3 = Pattern.compile("^### [^#]", Pattern.MULTILINE).matcher(readme).find();
        if (hasH2 && hasH3) good.add("헤딩 계층"); else bad.add("헤딩 계층");

        int cbCount = 0;
        Matcher cbMatcher = CODE_BLOCK.matcher(readme);
        while (cbMatcher.find()) cbCount++;
        if (cbCount >= 2) good.add("코드 블록"); else bad.add("코드 블록");

        if (LIST_OR_TABLE.matcher(readme).find()) good.add("목록/표"); else bad.add("목록/표");

        boolean hasSkip = Pattern.compile("^# [^#].*\\n(?:(?!^#).*\\n)*^### ", Pattern.MULTILINE)
                .matcher(readme).find();
        if (!hasSkip) good.add("헤딩 연속성"); else bad.add("헤딩 연속성");

        return new SubItemDetail(List.copyOf(good), List.copyOf(bad));
    }

    /** 재현성 코드분 세부 항목별 충족/불충족 */
    public static SubItemDetail reproducibilityCodeDetail(String readme) {
        List<String> good = new ArrayList<>();
        List<String> bad = new ArrayList<>();

        if (VERSION.matcher(readme).find()) good.add("버전 명시"); else bad.add("버전 명시");
        if (DEPS.matcher(readme).find()) good.add("의존성 설치"); else bad.add("의존성 설치");
        if (RUN_CMD.matcher(readme).find()) good.add("실행 명령어"); else bad.add("실행 명령어");

        return new SubItemDetail(List.copyOf(good), List.copyOf(bad));
    }

    /** 가독성 0~4점 */
    public static int analyzeReadability(String readme) {
        return readabilityDetail(readme).good().size();
    }

    /** 시각자료 0/1점 */
    public static int analyzeVisual(String readme) {
        Matcher matcher = IMG_PATTERN.matcher(readme);
        while (matcher.find()) {
            String url = matcher.group(1).toLowerCase();
            boolean isBadge = BADGE_DOMAINS.stream().anyMatch(url::contains);
            if (!isBadge) return 1;
        }
        return 0;
    }

    /** 재현성 코드분 0~3점 */
    public static int analyzeReproducibilityCode(String readme) {
        return reproducibilityCodeDetail(readme).good().size();
    }

    /** 라이선스 0/1점 */
    public static int analyzeLicense(String licenseField) {
        return (licenseField != null && !licenseField.isBlank()) ? 1 : 0;
    }
}