package com.nesterukia.blog.integration.web.comments;

import com.nesterukia.blog.integration.web.BaseWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CreateCommentsTest extends BaseWebIntegrationTest {
    @Test
    void addCommentSuccess() throws Exception {
        Long postId = createPostAndGetId();
        String createCommentJson = """
        {
          "text": "Комментарий к посту",
          "postId": %d
        }
        """.formatted(postId);

        String addCommentUri = String.format(COMMENTS_URI, postId);

        mockMvc.perform(MockMvcRequestBuilders.post(addCommentUri)
                        .content(createCommentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("Комментарий к посту"))
                .andExpect(jsonPath("$.postId").value(postId));
    }

    @Test
    void addCommentMissingText() throws Exception {
        Long postId = createPostAndGetId();
        String createCommentJson = """
        {
          "postId": %d
        }
        """.formatted(postId);

        String addCommentUri = String.format(COMMENTS_URI, postId);

        mockMvc.perform(MockMvcRequestBuilders.post(addCommentUri)
                        .content(createCommentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCommentMissingPostId() throws Exception {
        Long postId = createPostAndGetId();
        String createCommentJson = """
        {
          "text": "Комментарий к посту"
        }
        """;

        String addCommentUri = String.format(COMMENTS_URI, postId);

        mockMvc.perform(MockMvcRequestBuilders.post(addCommentUri)
                        .content(createCommentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCommentToNonExistingPost() throws Exception {
        String createCommentJson = """
        {
          "text": "Комментарий к посту",
          "postId": 999
        }
        """;

        String addCommentUri = String.format(COMMENTS_URI, 999L);

        mockMvc.perform(MockMvcRequestBuilders.post(addCommentUri)
                        .content(createCommentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
