package org.apiteria.project.entity;

public record GitRepoResponse(String name,
                              Boolean fork,
                              Owner owner) {
}
