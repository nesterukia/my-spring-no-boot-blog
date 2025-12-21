package com.nesterukia.blog.integration.dao;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:test-application.properties")
public class BaseDaoIntegrationTest {
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    protected void setUp() {
        jdbcTemplate.execute("""
            DELETE FROM comments;
            DELETE FROM images;
            DELETE FROM post_tags;
            DELETE FROM posts;
            DELETE FROM tags;
        """);
    }
}
