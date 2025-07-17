package org.apiteria.project.services;


import org.apiteria.project.repositories.GithubDataRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GitService {

    private final Environment env;
    private final RestTemplate restTemplate;
    private final GithubDataRepository githubDataRepository;
    final Logger logger = LoggerFactory.getLogger(GitService.class);

    GitService(RestTemplate restTemplate,
               Environment environment,
               GithubDataRepository githubDataRepository) {

        this.restTemplate = restTemplate;
        this.env = environment;
        this.githubDataRepository = githubDataRepository;

        if (env.getProperty("api.gittoken").isBlank()) {
            logger.warn("No API key provided consider api key for more requestes");
        }
    }

    public List<Map<String, Object>> getReposAsList(String nickname) throws Exception {
        List<Map<String, Object>> repos = null;
        boolean ifExists = githubDataRepository.checkIfExists(nickname);

        if(ifExists){
            repos = githubDataRepository.getByName(nickname);
        }
        if(repos != null){
            logger.warn("zwrocona bez fetchowania");
            return repos;
        }

        String url = "https://api.github.com/users/{nickname}/repos";

        HttpEntity<String> entity = getAuthorizationIfGitTokenExists();
        //System.out.println(gitTokenOptional.isPresent());


        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                },
                nickname
        );

        repos = response.getBody();

        repos = filterDataForNotForkedRepos(repos);
        addBranchesToRepos(repos, nickname);
        if(ifExists){
            githubDataRepository.updateByName(nickname,repos);
        }else{
            githubDataRepository.insert(nickname,repos);
        }
        return repos;
    }

    private List<Map<String, Object>> filterDataForNotForkedRepos(List<Map<String, Object>> repos) {
        return repos.stream().filter(repo ->
                repo.containsKey("fork")).filter(repo ->
                repo.get("fork").toString().equals("false")).map(repo ->
        {
            HashMap<String, Object> owner = (HashMap<String, Object>) repo.get("owner");

            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("login", owner.get("login"));
            data.put("repositoryName", repo.get("name"));

            return data;
        }).collect(Collectors.toList());

    }

    private void addBranchesToRepos(List<Map<String, Object>> repos, String nickname) {
        repos.parallelStream().forEach(repo -> {
            repo.put("branches", getBranchesFromRepo(nickname, repo.get("repositoryName").toString()));
        });
    }

    private List<Map<String, Object>> getBranchesFromRepo(String login, String repo) {
        String url = "https://api.github.com/repos/{login}/{repo}/branches";

        HttpEntity<String> entity = getAuthorizationIfGitTokenExists();

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                },
                login,
                repo
        );
        List<Map<String, Object>> data = response.getBody();
        return data.stream().map(branch -> {
            HashMap<String, Object> commit = (HashMap<String, Object>) (branch.get("commit"));
            return Map.of(
                    "name", branch.get("name"),
                    "sha", commit.get("sha"));
        }).collect(Collectors.toList());
    }

    private HttpEntity<String> getAuthorizationIfGitTokenExists() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = null;
        if (!env.getProperty("api.gittoken").isBlank()) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + env.getProperty("api.gittoken"));
            entity = new HttpEntity<String>(headers);

        }
        return entity;
    }
}
