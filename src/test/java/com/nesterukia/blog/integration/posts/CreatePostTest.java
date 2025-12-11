package com.nesterukia.blog.integration.posts;

import com.nesterukia.blog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CreatePostTest extends BaseIntegrationTest {

    @Test
    void createPostNoTagsIsOk() throws Exception {
        String expectedTitle = "Post title 1";
        String expectedText = "Post text 1";
        int expectedLikesCount = 0;
        int expectedCommentsCount = 0;

        String createPostJson = String.format("""
        {
          "title": "%s",
          "text": "%s",
          "tags": []
        }
        """, expectedTitle, expectedText);

        MockHttpServletRequestBuilder request = post(POSTS_URI)
                .content(createPostJson)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(expectedTitle))
                .andExpect(jsonPath("$.text").value(expectedText))
                .andExpect(jsonPath("$.likesCount").value(expectedLikesCount))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.commentsCount").value(expectedCommentsCount));
    }
}
