package com.nesterukia.blog.integration.web.comments;

import com.nesterukia.blog.integration.web.BaseWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UpdateCommentsTest extends BaseWebIntegrationTest {

    @Test
    void updateCommentSuccess() throws Exception {
        String initCommentText = "Init comment text";
        Long postId = createPostAndGetId();
        Long commentId = createCommentAndGetId(postId, initCommentText);

        String updateCommentJson = """
        {
          "id": %d,
          "text": "Updated text",
          "postId": %d
        }
        """.formatted(commentId, postId);

        String updateCommentUri = String.format(SINGLE_COMMENT_URI, postId, commentId);

        mockMvc.perform(MockMvcRequestBuilders.put(updateCommentUri)
                        .content(updateCommentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Updated text"))
                .andExpect(jsonPath("$.postId").value(postId));
    }

    @Test
    void updateCommentMissingText() throws Exception {
        String initCommentText = "Init comment text";
        Long postId = createPostAndGetId();
        Long commentId = createCommentAndGetId(postId, initCommentText);

        String updateCommentJson = """
        {
          "id": %d,
          "postId": %d
        }
        """.formatted(commentId, postId);

        String updateCommentUri = String.format(SINGLE_COMMENT_URI, postId, commentId);

        mockMvc.perform(MockMvcRequestBuilders.put(updateCommentUri)
                        .content(updateCommentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCommentNotFound() throws Exception {
        Long postId = createPostAndGetId();

        String updateCommentJson = """
        {
          "id": 999,
          "text": "Updated text",
          "postId": %d
        }
        """.formatted(postId);

        String updateCommentUri = String.format(SINGLE_COMMENT_URI, postId, 999L);

        mockMvc.perform(MockMvcRequestBuilders.put(updateCommentUri)
                        .content(updateCommentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCommentFromWrongPost() throws Exception {
        String initCommentText = "Init comment text";
        Long postId = createPostAndGetId();
        Long commentId = createCommentAndGetId(postId, initCommentText);

        String updateCommentJson = """
        {
          "id": %d,
          "text": "Updated text",
          "postId": %d
        }
        """.formatted(commentId, postId);

        String updateCommentUri = String.format(SINGLE_COMMENT_URI, 999L, commentId);

        mockMvc.perform(MockMvcRequestBuilders.put(updateCommentUri)
                        .content(updateCommentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
