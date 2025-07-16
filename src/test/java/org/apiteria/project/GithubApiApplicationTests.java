package org.apiteria.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class GithubApiApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;
    @Test
    void should_returnNonForkedRepositoriesWithBranches_forExistingUser() {
        // --- GIVEN ---

        String nickname = "PsaiduPL";
        String url = "/api/" + nickname;

        ParameterizedTypeReference<List<Map<String, Object>>> responseType =
                new ParameterizedTypeReference<>() {};

        // --- WHEN ---
        ResponseEntity<List<Map<String, Object>>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                responseType
        );

        // --- THEN ---
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> repositories = responseEntity.getBody();
        assertThat(repositories).isNotNull();
        assertThat(repositories).isNotEmpty();

        for (Map<String, Object> repo : repositories) {

            assertThat(repo).containsKeys("login", "repositoryName", "branches");



            assertThat(repo.get("login")).isEqualTo(nickname);
            assertThat(repo.get("repositoryName")).isInstanceOf(String.class).isNotNull();


            Object branchesObject = repo.get("branches");
            assertThat(branchesObject).isInstanceOf(List.class);
            List<Map<String, Object>> branches = (List<Map<String, Object>>) branchesObject;

            assertThat(branches).isNotEmpty();

            Map<String, Object> firstBranch = branches.get(0);
            assertThat(firstBranch).containsKeys("name", "sha");
            assertThat(firstBranch.get("name")).isInstanceOf(String.class).isNotNull();
            assertThat(firstBranch.get("sha")).isInstanceOf(String.class).isNotNull();
        }
    }
}