package com.nesterukia.blog.repository.implementations.jdbcNative;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class JdbcNativeRepository {
    protected final JdbcTemplate jdbcTemplate;

    public JdbcNativeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
