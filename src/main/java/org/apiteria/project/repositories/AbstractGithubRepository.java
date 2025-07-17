package org.apiteria.project.repositories;


public interface AbstractGithubRepository<T>{

    T getByName(String name);
    void updateByName(String name,T obj);
    void deleteByName(String name);
    Long getIdByName(String name);
    void insert(String name,T obj);
    boolean checkIfExists(String name);
}
