package org.apiteria.project.services;


import lombok.AllArgsConstructor;
import org.apiteria.project.aspects.Cached;
import org.apiteria.project.entity.Branch;
import org.apiteria.project.entity.GitBranchResponse;
import org.apiteria.project.entity.GitRepoResponse;
import org.apiteria.project.entity.Repo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GitService {



    private final GitAdapter gitAdapter;

    @Cacheable(value = "cached-repos", key = "#nickname")
    public List<Repo> getReposAsList(String nickname) throws Exception {

        var repos = gitAdapter.getRepos(nickname);

        repos = filterDataForNotForkedRepos(repos);

        return addBranchesToRepos(repos);
    }

    private List<GitRepoResponse> filterDataForNotForkedRepos(List<GitRepoResponse > repos) {
        return repos.stream().filter(r->r.fork().equals(false)).toList();

    }

    private List<Repo> addBranchesToRepos(List<GitRepoResponse> repos) {
        return repos.parallelStream().map(repo -> {
           return new Repo(repo.owner().login(),repo.name(),
               gitAdapter.getBranchesFromRepo(repo.owner().login(),repo.name()).stream().map(this::mapBranchResponse).toList());
        }).toList();
    }
    private Branch mapBranchResponse(GitBranchResponse branchResponse) {
        return new Branch(branchResponse.name(),branchResponse.commit().sha());
    }



}
