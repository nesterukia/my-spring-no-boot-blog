package com.nesterukia.blog.integration.web.comments;

import com.nesterukia.blog.integration.web.BaseWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeleteCommentsTest extends BaseWebIntegrationTest {

    @Test
    void deleteCommentSuccess() throws Exception {
        String initCommentText = "Init comment text";
        Long postId = createPostAndGetId();
        Long commentId = createCommentAndGetId(postId, initCommentText);

        String deleteCommentUri = String.format(SINGLE_COMMENT_URI, postId, commentId);

        mockMvc.perform(MockMvcRequestBuilders.delete(deleteCommentUri))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get(deleteCommentUri))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCommentNotFound() throws Exception {
        Long postId = createPostAndGetId();

        String deleteCommentUri = String.format(SINGLE_COMMENT_URI, postId, 999L);

        mockMvc.perform(MockMvcRequestBuilders.delete(deleteCommentUri))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCommentFromWrongPost() throws Exception {
        String initCommentText = "Init comment text";
        Long postId = createPostAndGetId();
        Long commentId = createCommentAndGetId(postId, initCommentText);

        String deleteCommentUri = String.format(SINGLE_COMMENT_URI, 999L, commentId);

        mockMvc.perform(MockMvcRequestBuilders.delete(deleteCommentUri))
                .andExpect(status().isNotFound());
    }
}
