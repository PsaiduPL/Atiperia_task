package org.apiteria.project.controllers;


import org.apiteria.project.GithubApiApplication;
import org.apiteria.project.aspects.Cached;
import org.apiteria.project.entity.Repo;
import org.apiteria.project.services.GitService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RepoController {
    GitService gitService;

    public RepoController(GitService gitService) {
        this.gitService = gitService;
    }

//    @Cached(size =5 ,
//    refreshUnit = ChronoUnit.SECONDS,
//    refreshInterval = 15)


    @GetMapping("/{nickname}")
    public ResponseEntity<List<Repo>> getAllUserDetails(@PathVariable String nickname)throws Exception {
         return ResponseEntity.ok(gitService.getReposAsList(nickname));
    }
}
