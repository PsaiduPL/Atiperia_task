package org.apiteria.project.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public class GithubDataRepository implements AbstractGithubRepository<List<Map<String, Object>>> {
    Logger logger = LoggerFactory.getLogger(GithubDataRepository.class);
    final JdbcTemplate jdbcTemplate;
    final ObjectMapper objectMapper;
    String dbUsers = """
            CREATE TABLE IF NOT EXISTS users(
                id BIGSERIAL PRIMARY KEY NOT NULL,
                name VARCHAR(150) UNIQUE NOT NULL
            )
            
            """;
    String dbUserData = """
            CREATE TABLE IF NOT EXISTS user_data(
              id BIGINT PRIMARY KEY REFERENCES users(id),
                repos JSONB,
                data_timestamp TIMESTAMP DEFAULT NOW()
            )
            """;

    public GithubDataRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;

        jdbcTemplate.execute(dbUsers);
        jdbcTemplate.execute(dbUserData);
    }


    @Override
    public List<Map<String, Object>> getByName(String name) {
        String sql = """
                SELECT repos FROM user_data WHERE data_timestamp > NOW() - interval '5 MINUTES' AND id = ? """;
        Long id = getIdByName(name);

        try {
            String json = jdbcTemplate.queryForObject(sql, String.class, id);
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (DataAccessException | JsonProcessingException e) {
            return null;
        }

    }

    @Override
    public boolean checkIfExists(String name) {
        if (getIdByName(name) != null) {
            return true;

        }


        return false;


    }

    @Override
    public void updateByName(String name, List<Map<String, Object>> obj) {
        String sql = """
                UPDATE user_data SET repos = ?::jsonb, data_timestamp = NOW() WHERE id = ?""";
        try {

            jdbcTemplate.update(sql, objectMapper.writeValueAsString(obj), getIdByName(name));
        } catch (JsonProcessingException e) {
            logger.error("updateByName ", e);
        }
    }

    @Override
    public void deleteByName(String name) {

    }

    @Override
    public Long getIdByName(String name) {
        String sql = """
                SELECT id FROM users WHERE name = ?
                """;
        try {

            return jdbcTemplate.queryForObject(sql, (rs, row) -> {
                return rs.getLong("id");
            }, name);
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void insert(String name, List<Map<String, Object>> obj) {
        String sqlInsertUsr = "INSERT INTO users (name) VALUES(?)";
        String sqlInsertData = "INSERT INTO user_data (id, repos) VALUES(?, ?::jsonb)";

        try {
            String jsonObj = objectMapper.writeValueAsString(obj);


            Long id = getIdByName(name);
            if (id == null) {
                jdbcTemplate.update(sqlInsertUsr, name);
                id = getIdByName(name);
                jdbcTemplate.update(sqlInsertData, id, jsonObj);
            }


        } catch (JsonProcessingException e) {
            logger.error("JSON serialization error in insert", e);
            throw new RuntimeException("Could not serialize repos data", e); // rollback
        } catch (DataAccessException e) {
            logger.error("Database error in insert", e);
            throw e; // causes rollback
        }
    }
}
