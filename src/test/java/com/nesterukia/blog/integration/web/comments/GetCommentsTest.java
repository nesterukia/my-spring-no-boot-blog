package com.nesterukia.blog.integration.web.comments;

import com.nesterukia.blog.integration.web.BaseWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetCommentsTest extends BaseWebIntegrationTest {
    @Test
    void getAllCommentsSuccess() throws Exception {
        Long postId = createPostAndGetId();

        String firstCommentText = "My comment 1";
        createCommentAndGetId(postId, firstCommentText);

        String secondCommentText = "My comment 1";
        createCommentAndGetId(postId, secondCommentText);

        String getAllCommentsUri = String.format(COMMENTS_URI, postId);

        mockMvc.perform(MockMvcRequestBuilders.get(getAllCommentsUri))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].postId").value(hasItems(postId.intValue(), postId.intValue())))
                .andExpect(jsonPath("$[*].text").value(hasItems(
                        firstCommentText,
                        secondCommentText
                )));
    }

    @Test
    void getAllCommentsEmptyList() throws Exception {
        Long postId = createPostAndGetId();
        String getAllCommentsUri = String.format(COMMENTS_URI, postId);

        mockMvc.perform(MockMvcRequestBuilders.get(getAllCommentsUri))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllCommentsNonExistingPost() throws Exception {
        String getAllCommentsUri = String.format(COMMENTS_URI, 999L);

        mockMvc.perform(MockMvcRequestBuilders.get(getAllCommentsUri))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCommentByIdSuccess() throws Exception {
        String commentText = "My comment";
        Long postId = createPostAndGetId();
        Long commentId = createCommentAndGetId(postId, commentText);

        String getCommentUri = String.format(SINGLE_COMMENT_URI, postId, commentId);

        mockMvc.perform(MockMvcRequestBuilders.get(getCommentUri))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value(commentText))
                .andExpect(jsonPath("$.postId").value(postId));
    }

    @Test
    void getCommentByIdNotFound() throws Exception {
        Long postId = createPostAndGetId();
        String getCommentUri = String.format(SINGLE_COMMENT_URI, postId, 999L);
        mockMvc.perform(MockMvcRequestBuilders.get(getCommentUri))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCommentFromWrongPost() throws Exception {
        String commentText = "My comment";
        Long postId = createPostAndGetId();
        Long commentId = createCommentAndGetId(postId, commentText);
        String getCommentUri = String.format(SINGLE_COMMENT_URI, 999L, commentId);
        mockMvc.perform(MockMvcRequestBuilders.get(getCommentUri))
                .andExpect(status().isNotFound());
    }
}
