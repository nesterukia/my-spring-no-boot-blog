package com.nesterukia.blog.integration.web.posts;

import com.nesterukia.blog.integration.web.BaseWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CreatePostTest extends BaseWebIntegrationTest {

    @Test
    void createPostEmptyTagsIsOk() throws Exception {
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

    @Test
    void createPostWithTagsIsOk() throws Exception {
        String expectedTitle = "Post title 1";
        String expectedText = "Post text 1";
        String firstExpectedTag = "Tag_1";
        String secondExpectedTag = "Tag_2";
        int expectedLikesCount = 0;
        int expectedCommentsCount = 0;

        String createPostJson = String.format("""
        {
          "title": "%s",
          "text": "%s",
          "tags": ["%s", "%s"]
        }
        """, expectedTitle, expectedText, firstExpectedTag, secondExpectedTag);

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
                .andExpect(jsonPath("$.tags[0]").value(firstExpectedTag))
                .andExpect(jsonPath("$.tags[1]").value(secondExpectedTag))
                .andExpect(jsonPath("$.commentsCount").value(expectedCommentsCount));
    }

    @Test
    void createPostAbsentTitleThrowsException() throws Exception {
        String createPostJson = """
        {
          "text": "Post text",
          "tags": []
        }
        """;

        var createPostRequest = MockMvcRequestBuilders.post(POSTS_URI)
                .content(createPostJson)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(createPostRequest).andExpect(status().isBadRequest());
    }

    @Test
    void createPostAbsentTextThrowsException() throws Exception {
        String createPostJson = """
        {
          "title": "Post Title",
          "tags": []
        }
        """;

        var createPostRequest = MockMvcRequestBuilders.post(POSTS_URI)
                .content(createPostJson)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(createPostRequest).andExpect(status().isBadRequest());
    }

    @Test
    void createPostAbsentTagsThrowsException() throws Exception {
        String createPostJson = """
        {
          "title": "Post Title",
          "text": "Post text"
        }
        """;

        var createPostRequest = MockMvcRequestBuilders.post(POSTS_URI)
                .content(createPostJson)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(createPostRequest).andExpect(status().isBadRequest());
    }
}
