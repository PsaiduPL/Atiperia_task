package org.apiteria.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.apiteria.project.entity.*;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GithubApiApplicationTests {

    @Autowired
    private WebTestClient webTestClient;


    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension
            .newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();


    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {

        registry.add("github.baseurl",wireMock::baseUrl);
    }

    @Test
    void should_returnNonForkedRepositoriesWithBranches_forExistingUser() {
        // --- GIVEN ---
        String username = "PsaiduPL";
        String repoName = "Atiperia_task";


        wireMock.stubFor(
                WireMock.get(urlPathMatching("/users/[^/]+/repos")) // Używamy ścieżki względnej
                        .willReturn(aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        //.withBodyFile("github-repos-response.json") // Lepsza praktyka: trzymaj JSON w pliku
                                // Alternatywnie, tak jak u Ciebie:

                                .withBody(
                                        """
                                        [
                                          {
                                            "name": "Atiperia_task",
                                            "owner": { "login": "PsaiduPL" },
                                            "fork": false
                                           
                                          },
                                          {
                                            "name": "book-1",
                                            "owner": { "login": "PsaiduPL" },
                                            "fork": true
                                          
                                          }
                                        ]
                                        """
                                ).withStatus(HttpStatus.OK.value())

                        )
        );


        wireMock.stubFor(
                WireMock.get(urlPathEqualTo("/repos/PsaiduPL/Atiperia_task/branches"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(
                                        """
                                        [
                                          {
                                            "name": "main",
                                            "commit": { "sha": "a1b2c3d4e5f6" }
                                          },
                                          {
                                            "name": "develop",
                                            "commit": { "sha": "f6e5d4c3b2a1" }
                                          }
                                        ]
                                        """
                                ).withStatus(HttpStatus.OK.value())
                        )
        );

        // --- WHEN & THEN ---
        webTestClient.get()
                .uri("/api/" + username)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Repo.class) // Używamy naszego DTO
                .value(repositories -> {
                    // Sprawdzamy, czy aplikacja poprawnie odfiltrowała forki
                    assertThat(repositories).hasSize(1);

                    Repo repo = repositories.get(0);
                    assertThat(repo.repositoryName()).isEqualTo(repoName);
                    assertThat(repo.login()).isEqualTo(username);

                    // Sprawdzamy gałęzie
                    assertThat(repo.branches()).hasSize(2);
                    assertThat(repo.branches())
                            .extracting(Branch::name, Branch::sha)
                            .containsExactlyInAnyOrder(
                                    org.assertj.core.groups.Tuple.tuple( "main","a1b2c3d4e5f6"),
                                    org.assertj.core.groups.Tuple.tuple( "develop","f6e5d4c3b2a1")
                            );
                });
    }
}