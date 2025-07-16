package org.apiteria.project.controllers;


import org.apiteria.project.GithubApiApplication;
import org.apiteria.project.services.GitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RepoController {
    GitService gitService;

    RepoController(GitService gitService) {
        this.gitService = gitService;
    }


    @GetMapping("/{nickname}")
    ResponseEntity<List<Map<String, Object>>> getAllUserDetails(@PathVariable String nickname)throws Exception {
         return ResponseEntity.ok(gitService.getReposAsList(nickname));
    }
}
