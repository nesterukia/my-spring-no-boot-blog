package com.nesterukia.blog.integration.web.posts;

import com.nesterukia.blog.integration.web.BaseWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GetPostTest extends BaseWebIntegrationTest {

    @Test
    void getAllPostsWithSearchIsOk() throws Exception {
        createPostAndGetId("Post 1 with search", "Unique text 1");
        createPostAndGetId("Post 2 with search", "Unique text 2");
        createPostAndGetId("Other post", "Text 3");

        String request = POSTS_URI + "?search=Unique&pageNumber=1&pageSize=10";

        mockMvc.perform(get(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.posts[0].title").exists())
                .andExpect(jsonPath("$.posts[0].likesCount").exists())
                .andExpect(jsonPath("$.posts[0].commentsCount").exists())
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.lastPage").value(1));
    }

    @Test
    void getAllPostsPaginationIsOk() throws Exception {
        for (int i = 1; i <= 15; i++) {
            createPostAndGetId("Post " + i, "Text " + i);
        }

        String request = POSTS_URI + "?search=&pageNumber=2&pageSize=5";

        mockMvc.perform(get(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(5))
                .andExpect(jsonPath("$.hasPrev").value(true))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.lastPage").value(3));
    }

    @Test
    void getPostByIdIsOk() throws Exception {
        String createPostJson = """
        {
          "title": "Test Post",
          "text": "Test text content",
          "tags": ["tag1", "tag2"]
        }
        """;

        String responseBody = mockMvc.perform(
                        MockMvcRequestBuilders.post(POSTS_URI)
                                .content(createPostJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(responseBody).get("id").asLong();

        String getRequest = String.format(SINGLE_POST_URI, postId);

        mockMvc.perform(get(getRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("Test Post"))
                .andExpect(jsonPath("$.text").value("Test text content"))
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.likesCount").exists())
                .andExpect(jsonPath("$.commentsCount").exists());
    }

    @Test
    void getPostByNonExistingIdThrowsException() throws Exception {
        String getRequest = String.format(SINGLE_POST_URI, 999L);

        mockMvc.perform(get(getRequest)).andExpect(status().isNotFound());
    }
}
