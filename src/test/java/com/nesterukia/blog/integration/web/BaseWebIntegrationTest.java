package com.nesterukia.blog.integration.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(locations = "classpath:test-application.properties")
public abstract class BaseWebIntegrationTest {
    @Autowired
    protected WebApplicationContext wac;
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected MockMvc mockMvc;

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected static final String POSTS_URI = "/api/posts";
    protected static final String SINGLE_POST_URI = "/api/posts/%d";
    protected static final String COMMENTS_URI = "/api/posts/%d/comments";
    protected static final String SINGLE_COMMENT_URI = "/api/posts/%d/comments/%d";
    protected static final String INCREMENT_LIKES_COUNT_URI = "/api/posts/%d/likes";
    protected static final String IMAGE_URI = "/api/posts/%d/image";

    @BeforeEach
    protected void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        jdbcTemplate.execute("""
            DELETE FROM comments;
            DELETE FROM images;
            DELETE FROM post_tags;
            DELETE FROM posts;
            DELETE FROM tags;
        """);
    }

    protected Long createPostAndGetId() throws Exception {
        return createPostAndGetId("Mock title", "Mock text");
    }

    protected Long createPostAndGetId(String title, String text) throws Exception {
        String createPostJson = String.format("""
        {
          "title": "%s",
          "text": "%s",
          "tags": []
        }
        """, title, text);

        var createPostRequest = MockMvcRequestBuilders.post(POSTS_URI)
                .content(createPostJson)
                .contentType(MediaType.APPLICATION_JSON);
        String responseBody = mockMvc.perform(createPostRequest)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody).get("id").asLong();
    }

    protected Long createCommentAndGetId(Long postId, String commentText) throws Exception {
        String createCommentJson = """
        {
          "text": "%s",
          "postId": %d
        }
        """.formatted(commentText, postId);

        var createCommentRequest = MockMvcRequestBuilders.post(String.format(COMMENTS_URI, postId))
                .content(createCommentJson)
                .contentType(MediaType.APPLICATION_JSON);

        String createCommentResponse = mockMvc.perform(createCommentRequest)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(createCommentResponse).get("id").asLong();
    }
}
