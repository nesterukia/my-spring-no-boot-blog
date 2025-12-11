package com.nesterukia.blog.integration;

import com.nesterukia.blog.WebConfiguration;
import com.nesterukia.blog.integration.config.TestDataSourceConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringJUnitConfig(classes = {
        TestDataSourceConfiguration.class,
        WebConfiguration.class
})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
public abstract class BaseIntegrationTest {
    @Autowired
    protected WebApplicationContext wac;
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected MockMvc mockMvc;

    protected static final String POSTS_URI = "/api/posts";
    protected static final String SINGLE_POST_URI = "/api/posts/%d";
    protected static final String COMMENTS_URI = "/api/posts/%d/comments";
    protected static final String SINGLE_COMMENT_URI = "/api/posts/%d/comments/%d";
    protected static final String INCREMENT_LIKES_COUNT_URI = "/api/posts/%d/likes";
    protected static final String IMAGE_URI = "/api/posts/%d/image";

    @BeforeEach
    protected void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM post_tags");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM images");
    }

}
