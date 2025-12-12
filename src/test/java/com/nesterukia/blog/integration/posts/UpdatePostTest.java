package com.nesterukia.blog.integration.posts;

import com.nesterukia.blog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdatePostTest extends BaseIntegrationTest {

    @Test
    void updatePostIsOk() throws Exception {
        String createPostJson = """
        {
          "title": "Original Title",
          "text": "Original text content",
          "tags": ["tag1"]
        }
        """;

        MockHttpServletRequestBuilder createRequest = MockMvcRequestBuilders.post(POSTS_URI)
                .content(createPostJson)
                .contentType(MediaType.APPLICATION_JSON);

        String createResponse = mockMvc.perform(createRequest)
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(createResponse).get("id").asLong();

        String updatePostJson = """
        {
          "id": %d,
          "title": "Updated Title",
          "text": "Updated text content",
          "tags": ["tag1", "tag2"]
        }
        """.formatted(postId);

        String updateRequestBody = String.format(SINGLE_POST_URI, postId);
        var updateRequest = MockMvcRequestBuilders.put(updateRequestBody)
                .content(updatePostJson)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(updateRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.text").value("Updated text content"))
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0));
    }

    @Test
    void updateNonExistingPostThrowsException() throws Exception {
        String updatePostJson = """
        {
          "id": 999,
          "title": "Updated Title",
          "text": "Updated text content",
          "tags": []
        }
        """;

        String updateRequest = String.format(SINGLE_POST_URI, 999L);

        mockMvc.perform(MockMvcRequestBuilders.put(updateRequest)
                        .content(updatePostJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePostChangeTagsIsOk() throws Exception {
        String createPostJson = """
        {
          "title": "Post Title",
          "text": "Post text",
          "tags": ["tag1"]
        }
        """;

        var createRequest = MockMvcRequestBuilders.post(POSTS_URI)
                .content(createPostJson)
                .contentType(MediaType.APPLICATION_JSON);
        String createResponse = mockMvc.perform(createRequest)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(createResponse).get("id").asLong();

        String updatePostJson = """
        {
          "id": %d,
          "title": "Post Title",
          "text": "Post text",
          "tags": ["tag2", "tag3"]
        }
        """.formatted(postId);

        String updateRequestBody = String.format(SINGLE_POST_URI, postId);
        var updateRequest = MockMvcRequestBuilders.put(updateRequestBody)
                .content(updatePostJson)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(updateRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags[0]").value("tag2"))
                .andExpect(jsonPath("$.tags[1]").value("tag3"));
    }

    @Test
    void updatePostAbsentTitleThrowsException() throws Exception {
        String createPostJson = """
        {
          "title": "Original Title",
          "text": "Original text",
          "tags": []
        }
        """;

        String createResponse = mockMvc.perform(
                        MockMvcRequestBuilders.post(POSTS_URI)
                                .content(createPostJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(createResponse).get("id").asLong();

        String updatePostJson = """
        {
          "id": %d,
          "text": "Updated text",
          "tags": []
        }
        """.formatted(postId);

        String updateRequest = String.format(SINGLE_POST_URI, postId);

        mockMvc.perform(MockMvcRequestBuilders.put(updateRequest)
                        .content(updatePostJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePostAbsentTextThrowsException() throws Exception {
        String createPostJson = """
        {
          "title": "Original Title",
          "text": "Original text",
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

        String updatePostJson = """
        {
          "id": %d,
          "title": "Updated Title",
          "tags": []
        }
        """.formatted(postId);

        String updateRequestBody = String.format(SINGLE_POST_URI, postId);
        var updateRequest = MockMvcRequestBuilders.put(updateRequestBody)
                .content(updatePostJson)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(updateRequest).andExpect(status().isBadRequest());
    }

    @Test
    void updatePostAbsentTagsThrowsException() throws Exception {
        String createPostJson = """
        {
          "title": "Original Title",
          "text": "Original text",
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

        String updatePostJson = """
        {
          "id": %d,
          "title": "Updated Title",
          "text": "Updated text"
        }
        """.formatted(postId);

        String updateRequestBody = String.format(SINGLE_POST_URI, postId);
        var updateRequest = MockMvcRequestBuilders.put(updateRequestBody)
                .content(updatePostJson)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(updateRequest).andExpect(status().isBadRequest());
    }
}
