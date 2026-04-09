#!/usr/bin/env python3
"""
GitHub 데이터 정합성 검증 스크립트

Spring 수집기가 저장한 DB 데이터와 실제 GitHub API 데이터를 비교합니다.

사용법:
    python verify_github_data.py <github_login> <year> [--token YOUR_TOKEN] [options]

예시:
    python verify_github_data.py lmatarodo 2026 --token ghp_xxxx
    python verify_github_data.py lmatarodo 2026 --token ghp_xxxx --db-host 127.0.0.1 --db-port 3306

DB 접속 방법:
    A. 서버에서 직접 실행  : --db-host 127.0.0.1 (기본값)
    B. 로컬에서 SSH 터널  : ssh -L 3307:127.0.0.1:3306 sosd-dev@SSA-DEV
                            그 후 --db-port 3307
"""

import argparse
import sys
import time
import re
from typing import Optional

# ── 선택적 의존성 ──────────────────────────────────────────────────────────────
try:
    import pymysql
    HAS_PYMYSQL = True
except ImportError:
    HAS_PYMYSQL = False

try:
    import requests
    HAS_REQUESTS = True
except ImportError:
    HAS_REQUESTS = False


# ══════════════════════════════════════════════════════════════════════════════
# 설정
# ══════════════════════════════════════════════════════════════════════════════

DB_DEFAULTS = {
    "host":     "127.0.0.1",
    "port":     3306,
    "user":     "root",
    "password": "",
    "database": "sosd",       # Spring 백엔드 DB 이름 (실제 환경에 맞게 수정)
    "charset":  "utf8mb4",
}

GITHUB_API_BASE = "https://api.github.com"

# GitHub REST API 커밋 수 조회 시 최대 페이지 수 (100 commits/page × 50 = 5,000)
MAX_COMMIT_PAGES = 50


# ══════════════════════════════════════════════════════════════════════════════
# DB 쿼리
# ══════════════════════════════════════════════════════════════════════════════

SQL_GET_GITHUB_ID = """
    SELECT github_id, github_login_username
    FROM github_account
    WHERE github_login_username = %s
    LIMIT 1
"""

SQL_CONTRIBUTION_STATS = """
    SELECT
        gr.owner_name,
        gr.repo_name,
        CONCAT(gr.owner_name, '/', gr.repo_name) AS full_name,
        cs.commit_count,
        cs.commit_lines,
        cs.pr_count,
        cs.issue_count,
        cs.guideline_score,
        cs.repo_score
    FROM github_contribution_stats cs
    JOIN github_repository gr ON cs.repo_id = gr.id
    WHERE cs.github_id = %s AND cs.year = %s
    ORDER BY cs.repo_score DESC
"""

# raw 테이블에서 직접 카운트 (집계 테이블 vs 원본 교차검증)
SQL_RAW_COMMIT_COUNT = """
    SELECT COUNT(*) AS cnt,
           COALESCE(SUM(gc.addition + gc.deletion), 0) AS lines
    FROM github_commit gc
    JOIN github_repository gr ON gc.repo_id = gr.id
    WHERE gc.github_id = %s
      AND CONCAT(gr.owner_name, '/', gr.repo_name) = %s
      AND YEAR(gc.author_date) = %s
"""

SQL_RAW_PR_COUNT = """
    SELECT COUNT(*) AS cnt
    FROM github_pull_request gpr
    JOIN github_repository gr ON gpr.repo_id = gr.id
    WHERE gpr.github_id = %s
      AND CONCAT(gr.owner_name, '/', gr.repo_name) = %s
      AND YEAR(gpr.pr_date) = %s
"""

SQL_RAW_ISSUE_COUNT = """
    SELECT COUNT(*) AS cnt
    FROM github_issue gi
    JOIN github_repository gr ON gi.repo_id = gr.id
    WHERE gi.github_id = %s
      AND CONCAT(gr.owner_name, '/', gr.repo_name) = %s
      AND YEAR(gi.issue_date) = %s
"""

# github_score 요약
SQL_SCORE = """
    SELECT total_score, commit_count, (total_addition + total_deletion) AS commit_lines,
           pr_count, issue_count, best_repo
    FROM spring_github_score
    WHERE github_id = %s AND year = %s
    LIMIT 1
"""


def get_db_connection(args):
    if not HAS_PYMYSQL:
        print("[ERROR] pymysql이 설치되지 않았습니다. pip install pymysql")
        sys.exit(1)
    return pymysql.connect(
        host=args.db_host,
        port=args.db_port,
        user=args.db_user,
        password=args.db_password,
        database=args.db_name,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )


def query_db_stats(conn, github_id: int, year: int):
    with conn.cursor() as cur:
        cur.execute(SQL_CONTRIBUTION_STATS, (github_id, year))
        stats = cur.fetchall()

        cur.execute(SQL_SCORE, (github_id, year))
        score = cur.fetchone()

    return stats, score


def query_raw_counts(conn, github_id: int, full_name: str, year: int):
    with conn.cursor() as cur:
        cur.execute(SQL_RAW_COMMIT_COUNT, (github_id, full_name, year))
        commit_row = cur.fetchone()
        cur.execute(SQL_RAW_PR_COUNT, (github_id, full_name, year))
        pr_row = cur.fetchone()
        cur.execute(SQL_RAW_ISSUE_COUNT, (github_id, full_name, year))
        issue_row = cur.fetchone()

    return {
        "commit_count": commit_row["cnt"],
        "commit_lines": commit_row["lines"],
        "pr_count":     pr_row["cnt"],
        "issue_count":  issue_row["cnt"],
    }


# ══════════════════════════════════════════════════════════════════════════════
# GitHub REST API
# ══════════════════════════════════════════════════════════════════════════════

def make_headers(token: Optional[str]) -> dict:
    h = {"Accept": "application/vnd.github.v3+json"}
    if token:
        h["Authorization"] = f"Bearer {token}"
    return h


def _get_last_page(link_header: str) -> int:
    """Link 헤더에서 last page 번호 추출."""
    if not link_header:
        return 1
    match = re.search(r'page=(\d+)>; rel="last"', link_header)
    return int(match.group(1)) if match else 1


def github_commit_count(owner: str, repo: str, login: str, year: int,
                         headers: dict) -> Optional[int]:
    """
    GitHub REST API로 특정 저장소의 특정 연도 커밋 수 조회.
    per_page=1로 첫 요청 후 Link 헤더로 전체 페이지 수만 읽음 → API 호출 1회.
    """
    url = f"{GITHUB_API_BASE}/repos/{owner}/{repo}/commits"
    params = {
        "author":   login,
        "since":    f"{year}-01-01T00:00:00Z",
        "until":    f"{year + 1}-01-01T00:00:00Z",
        "per_page": 1,
    }
    try:
        resp = requests.get(url, headers=headers, params=params, timeout=15)
        if resp.status_code == 404:
            return None  # 비공개 레포 또는 존재하지 않는 레포
        if resp.status_code == 409:
            return 0    # 빈 레포
        resp.raise_for_status()

        data = resp.json()
        if not data:
            return 0

        link = resp.headers.get("Link", "")
        last_page = _get_last_page(link)
        if last_page > 1:
            return last_page  # per_page=1 → last_page = 전체 커밋 수

        return len(data)  # last_page == 1 → data에 전체 결과
    except requests.RequestException as e:
        print(f"  [API 오류] commit count ({owner}/{repo}): {e}")
        return None


def github_search_count(repo_full: str, login: str, year: int,
                        kind: str, headers: dict) -> Optional[int]:
    """
    GitHub Search API로 PR 또는 이슈 수 조회.
    kind: 'pr' 또는  'issue'
    """
    url = f"{GITHUB_API_BASE}/search/issues"
    date_range = f"{year}-01-01..{year}-12-31"
    q = f"repo:{repo_full} is:{kind} author:{login} created:{date_range}"
    try:
        resp = requests.get(url, headers=headers, params={"q": q, "per_page": 1}, timeout=15)
        if resp.status_code == 422:
            return None  # 비공개 레포 등 검색 불가
        resp.raise_for_status()
        return resp.json().get("total_count")
    except requests.RequestException as e:
        print(f"  [API 오류] {kind} count ({repo_full}): {e}")
        return None


# ══════════════════════════════════════════════════════════════════════════════
# 출력 헬퍼
# ══════════════════════════════════════════════════════════════════════════════

def _diff_str(db_val, api_val) -> str:
    if api_val is None:
        return "  N/A (비공개)"
    diff = db_val - api_val
    if diff == 0:
        return "  ✓"
    return f"  ✗ (DB:{db_val} / API:{api_val}, 차이:{diff:+d})"


def print_table_row(label: str, db_val, raw_val, api_val):
    raw_match = "✓" if db_val == raw_val else f"✗(raw={raw_val})"
    api_info = "N/A" if api_val is None else str(api_val)
    api_diff = ""
    if api_val is not None:
        d = db_val - api_val
        api_diff = " ✓" if d == 0 else f" ✗({d:+d})"
    print(f"  {label:<18} DB={db_val:>6}  raw={raw_match:<12}  API={api_info:>6}{api_diff}")


# ══════════════════════════════════════════════════════════════════════════════
# 메인 로직
# ══════════════════════════════════════════════════════════════════════════════

def run(args):
    if not HAS_REQUESTS:
        print("[ERROR] requests가 설치되지 않았습니다. pip install requests")
        sys.exit(1)

    login    = args.login
    year     = args.year
    token    = args.token
    headers  = make_headers(token)
    skip_api = args.skip_api

    # ── DB 접속 및 유저 조회 ──────────────────────────────────────────────────
    print(f"\n{'═'*65}")
    print(f"  GitHub 데이터 정합성 검증   login={login}  year={year}")
    print(f"{'═'*65}")

    conn = get_db_connection(args)
    with conn.cursor() as cur:
        cur.execute(SQL_GET_GITHUB_ID, (login,))
        row = cur.fetchone()

    if not row:
        print(f"[ERROR] DB에서 '{login}' 유저를 찾을 수 없습니다.")
        conn.close()
        sys.exit(1)

    github_id = row["github_id"]
    print(f"  DB github_id = {github_id}")

    # ── contribution_stats 조회 ───────────────────────────────────────────────
    stats, score = query_db_stats(conn, github_id, year)

    if not stats:
        print(f"\n  [정보] {year}년 contribution_stats 데이터가 없습니다.")
        conn.close()
        return

    if score:
        print(f"\n  [github_score] total={score['total_score']}  "
              f"commits={score['commit_count']}  lines={score['commit_lines']}  "
              f"pr={score['pr_count']}  issue={score['issue_count']}  "
              f"best_repo={score['best_repo']}")

    # ── 레포별 검증 ────────────────────────────────────────────────────────────
    total_mismatch = 0

    for s in stats:
        full_name = s["full_name"]
        owner, repo = s["owner_name"], s["repo_name"]

        print(f"\n  ┌─ {full_name}")

        # raw 테이블 교차검증
        raw = query_raw_counts(conn, github_id, full_name, year)

        # GitHub API 호출 (--skip-api 플래그로 생략 가능)
        if skip_api:
            api_commits = api_prs = api_issues = None
        else:
            # Rate limit 방지: Search API는 1분 30회 제한
            time.sleep(0.5)
            api_commits = github_commit_count(owner, repo, login, year, headers)
            time.sleep(1.5)  # Search API rate limit 여유
            api_prs     = github_search_count(full_name, login, year, "pr",    headers)
            time.sleep(1.5)
            api_issues  = github_search_count(full_name, login, year, "issue", headers)

        # 출력
        print_table_row("커밋 수",    s["commit_count"], raw["commit_count"], api_commits)
        print_table_row("커밋 라인수", s["commit_lines"], raw["commit_lines"], None)  # API로 검증 불가
        print_table_row("PR 수",      s["pr_count"],     raw["pr_count"],     api_prs)
        print_table_row("이슈 수",    s["issue_count"],  raw["issue_count"],  api_issues)

        # 불일치 집계
        for db_val, raw_val in [
            (s["commit_count"], raw["commit_count"]),
            (s["pr_count"],     raw["pr_count"]),
            (s["issue_count"],  raw["issue_count"]),
        ]:
            if db_val != raw_val:
                total_mismatch += 1

        if not skip_api:
            for db_val, api_val in [
                (s["commit_count"], api_commits),
                (s["pr_count"],     api_prs),
                (s["issue_count"],  api_issues),
            ]:
                if api_val is not None and db_val != api_val:
                    total_mismatch += 1

        print(f"  │  guideline_score={s['guideline_score']:.1f}  repo_score={s['repo_score']:.3f}")
        print(f"  └{'─'*50}")

    conn.close()

    # ── 요약 ──────────────────────────────────────────────────────────────────
    print(f"\n{'─'*65}")
    if total_mismatch == 0:
        print("  [결과] 모든 수치 일치 ✓")
    else:
        print(f"  [결과] 불일치 항목 {total_mismatch}개 발견 ✗")
        print("  ※ DB값 > API값: 아직 수집됐으나 GitHub에선 삭제/비공개 처리됨")
        print("  ※ DB값 < API값: 수집 누락 가능성 있음")
    print(f"{'─'*65}\n")


# ══════════════════════════════════════════════════════════════════════════════
# CLI
# ══════════════════════════════════════════════════════════════════════════════

def main():
    parser = argparse.ArgumentParser(
        description="Spring DB ↔ GitHub API 데이터 정합성 검증",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument("login", help="검증할 GitHub 로그인 username (예: lmatarodo)")
    parser.add_argument("year",  type=int, help="검증 연도 (예: 2026)")

    parser.add_argument("--token",       default=None,       help="GitHub Personal Access Token (없으면 rate limit 60회/h)")
    parser.add_argument("--skip-api",    action="store_true", help="GitHub API 호출 생략 (DB 내부 교차검증만)")

    # DB 연결
    db = parser.add_argument_group("DB 연결")
    db.add_argument("--db-host",     default="127.0.0.1", help="MySQL 호스트 (기본: 127.0.0.1)")
    db.add_argument("--db-port",     type=int, default=3306, help="MySQL 포트 (기본: 3306)")
    db.add_argument("--db-user",     default="root",      help="MySQL 유저 (기본: root)")
    db.add_argument("--db-password", default="",          help="MySQL 패스워드")
    db.add_argument("--db-name",     default="sosd",      help="DB 이름 (기본: sosd)")

    args = parser.parse_args()
    run(args)


if __name__ == "__main__":
    main()
