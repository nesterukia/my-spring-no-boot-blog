package com.nesterukia.blog.integration.web.images;

import com.nesterukia.blog.integration.web.BaseWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateImageTest extends BaseWebIntegrationTest {

    @Test
    void updateImageSuccess() throws Exception {
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

        byte[] imageBytes = "fake image content".getBytes();
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "image.jpg",
                "image/jpeg",
                imageBytes
        );
        String updateImageRequestUri = String.format(IMAGE_URI, postId);
        var updateImageRequest = MockMvcRequestBuilders.multipart(updateImageRequestUri)
                .file(imageFile)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                });
        mockMvc.perform(updateImageRequest).andExpect(status().isOk());
    }

    @Test
    void updateImageEmptyFileBadRequest() throws Exception {
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

        String updateImageRequest = String.format(IMAGE_URI, postId);
        MockMultipartFile emptyImageFile = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);

        mockMvc.perform(MockMvcRequestBuilders.multipart(updateImageRequest)
                        .file(emptyImageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateImageNonExistingPost() throws Exception {
        byte[] imageBytes = "fake image content".getBytes();
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "image.jpg",
                "image/jpeg",
                imageBytes
        );
        String updateImageRequest = String.format(IMAGE_URI, 999L);

        mockMvc.perform(MockMvcRequestBuilders.multipart(updateImageRequest)
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isNotFound());
    }
}
