package com.nesterukia.blog.integration.web.posts;

import com.nesterukia.blog.integration.web.BaseWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LikePostTest extends BaseWebIntegrationTest {

    @Test
    void incrementLikesCountIsOk() throws Exception {
        String createPostJson = """
        {
          "title": "Post Title",
          "text": "Post text",
          "tags": []
        }
        """;

        var createPostRequest = MockMvcRequestBuilders.post(POSTS_URI)
                .content(createPostJson)
                .contentType(MediaType.APPLICATION_JSON);

        String createResponse = mockMvc.perform(createPostRequest)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(createResponse).get("id").asLong();

        String incrementRequest = String.format(INCREMENT_LIKES_COUNT_URI, postId);

        mockMvc.perform(MockMvcRequestBuilders.post(incrementRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("1"));

        mockMvc.perform(MockMvcRequestBuilders.post(incrementRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void incrementLikesCountNonExistingPostThrowsException() throws Exception {
        String incrementRequest = String.format(INCREMENT_LIKES_COUNT_URI, 999L);

        mockMvc.perform(MockMvcRequestBuilders.post(incrementRequest))
                .andExpect(status().isNotFound());
    }

}
