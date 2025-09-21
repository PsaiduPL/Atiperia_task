package org.apiteria.project.services;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apiteria.project.entity.Branch;
import org.apiteria.project.entity.GitBranchResponse;
import org.apiteria.project.entity.GitRepoResponse;
import org.apiteria.project.entity.Repo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Log4j2
@Service
public class GitAdapter {

    @Value("${github.baseurl}")
    String baseUrl;
    private final RestClient restClient;
    private final Environment env;

    public GitAdapter(
        Environment environment,
        RestClient restClient
    ) {
        this.restClient = restClient;
        this.env = environment;

        if (env.getProperty("api.gittoken").isBlank()) {
            log.warn("No API key provided consider api key for more requestes");
        }
    }
    public List<GitRepoResponse> getRepos(String nickname){
        String url =baseUrl+ "/users/{nickname}/repos";

        ResponseEntity<List<GitRepoResponse>> response = restClient.get()
            .uri(url,nickname)
            .headers((HttpHeaders )->getAuthorizationIfGitTokenExists())
            .accept(MediaType.APPLICATION_JSON )
            .retrieve()

            .toEntity(new ParameterizedTypeReference<List<GitRepoResponse>>(){});
        return response.getBody();
    }

    public List<GitBranchResponse> getBranchesFromRepo(String login, String repoName) {
        String url = baseUrl + "/repos/{login}/{repoName}/branches";

        ResponseEntity<List<GitBranchResponse>> response = restClient.get()
            .uri(url,login,repoName)
            .headers((HttpHeaders )->getAuthorizationIfGitTokenExists())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<GitBranchResponse>>(){});

        return response.getBody();
    }
    private HttpHeaders getAuthorizationIfGitTokenExists() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = null;
        if (!env.getProperty("api.gittoken").isBlank()) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + env.getProperty("api.gittoken"));
            entity = new HttpEntity<String>(headers);

        }
        return headers;
    }

}
