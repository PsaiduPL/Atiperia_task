package org.apiteria.project.entity;

import java.util.List;

public record Repo(String login, String repositoryName, List<Branch> branches ) {
}
