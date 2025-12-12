package com.nesterukia.blog.integration.posts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nesterukia.blog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeletePostTest extends BaseIntegrationTest {

    @Test
    public void deleteExistingPostIsOk() throws Exception {
        String createPostJson = """
        {
          "title": "Title 1",
          "text": "Text 1",
          "tags": []
        }
        """;

        MockHttpServletRequestBuilder createRequest = post(POSTS_URI)
                .content(createPostJson)
                .contentType(MediaType.APPLICATION_JSON);

        String responseBody = mockMvc.perform(createRequest)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long postId = jsonNode.get("id").asLong();

        MockHttpServletRequestBuilder deleteRequest = delete(String.format(SINGLE_POST_URI, postId));
        mockMvc.perform(deleteRequest).andExpect(status().isOk());

        MockHttpServletRequestBuilder getPostRequest = get(String.format(SINGLE_POST_URI, postId));
        mockMvc.perform(getPostRequest).andExpect(status().isNotFound());
    }

    @Test
    public void deleteNonExistingPostThrowsException() throws Exception {
        Long nonExistingPostId = 1234L;
        MockHttpServletRequestBuilder deleteRequest = delete(String.format(SINGLE_POST_URI, nonExistingPostId));
        mockMvc.perform(deleteRequest).andExpect(status().isNotFound());
    }
}
