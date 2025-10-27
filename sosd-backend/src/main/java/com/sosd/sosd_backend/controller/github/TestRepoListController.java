package com.sosd.sosd_backend.controller.github;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.service.github.RepoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class TestRepoListController {

    private final RepoQueryService repoQueryService;

    @GetMapping(value = "/test/repoList", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String repoListPage(@RequestParam(name = "username", required = false) String username) {
        var sb = new StringBuilder();
        sb.append("<!doctype html><html><head><meta charset='utf-8'><title>Repo List</title></head><body>");
        sb.append("<h1>Repo List</h1>");
        sb.append("<form method='get' action='/test/repoList'>");
        sb.append(" GitHub username: <input name='username' value='")
                .append(username == null ? "" : escape(username)).append("'/>");
        sb.append(" <button type='submit'>Search</button>");
        sb.append("</form><hr/>");

        if (username == null || username.isBlank()) {
            sb.append("<p>username 쿼리 파라미터를 입력하세요. 예) /test/repoList?username=octocat</p>");
            sb.append("</body></html>");
            return sb.toString();
        }

        try {
            var repos = repoQueryService.listReposByLoginUsername(username);
            if (repos.isEmpty()) {
                sb.append("<p>연결된 레포가 없습니다.</p>");
            } else {
                sb.append("<p>username: <b>").append(escape(username))
                        .append("</b>, 연결된 레포 수: <b>").append(repos.size()).append("</b></p>");
                sb.append("<ol>");
                var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (GithubRepositoryEntity r : repos) {
                    sb.append("<li>")
                            .append(escape(r.getFullName()))
                            .append(" — private: ").append(Boolean.TRUE.equals(r.getIsPrivate()))
                            .append(" — updated_at: ").append(r.getGithubRepositoryUpdatedAt() == null ? "-"
                                    : fmt.format(r.getGithubRepositoryUpdatedAt()))
                            .append("</li>");
                }
                sb.append("</ol>");
            }
        } catch (IllegalArgumentException e) {
            sb.append("<p style='color:red'>").append(escape(e.getMessage())).append("</p>");
        } catch (Exception e) {
            sb.append("<p style='color:red'>알 수 없는 오류가 발생했습니다.</p>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    // 아주 간단한 HTML escape
    private static String escape(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }
}
