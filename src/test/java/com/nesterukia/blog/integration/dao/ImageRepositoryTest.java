package com.nesterukia.blog.integration.dao;

import com.nesterukia.blog.integration.config.TestDataSourceConfiguration;
import com.nesterukia.blog.model.Image;
import com.nesterukia.blog.repository.ImageRepository;
import com.nesterukia.blog.repository.implementations.jdbcNative.JdbcNativeImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringJUnitConfig(classes = {
        TestDataSourceConfiguration.class,
        JdbcNativeImageRepository.class
})
public class ImageRepositoryTest extends BaseDaoIntegrationTest {
    @Autowired
    private ImageRepository imageRepository;

    @Test
    void save_shouldAddImageToDatabase() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Image image = new Image();
        image.setSource("/app/storage/images/image-of-post-1.jpg");
        image.setPostId(1L);

        Image saved = imageRepository.save(image);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("/app/storage/images/image-of-post-1.jpg", saved.getSource());
        assertEquals(1L, saved.getPostId());

        Optional<Image> fromDb = imageRepository.findByPostId(1L);
        assertTrue("Image is not present in db", fromDb.isPresent());
        assertEquals("/app/storage/images/image-of-post-1.jpg", fromDb.get().getSource());
    }

    @Test
    void save_shouldGenerateAutoIncrementId() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Image image = new Image();
        image.setSource("/app/storage/images/new-image.jpg");
        image.setPostId(1L);

        Image saved1 = imageRepository.save(image);

        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                2L, "Post 2", "Text 2", 5L
        );

        Image image2 = new Image();
        image2.setSource("/app/storage/images/another-image.jpg");
        image2.setPostId(2L);

        Image saved2 = imageRepository.save(image2);

        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotEquals(saved1.getId(), saved2.getId());
    }

    @Test
    void findByPostId_shouldReturnImageWhenExists() {
        Long postId = 1L;
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO images (id, source, post_id) VALUES (?,?,?)",
                1L, "/app/storage/images/image-of-post-1.jpg", 1L
        );

        Optional<Image> image = imageRepository.findByPostId(postId);

        assertTrue("Image is not present in db", image.isPresent());
        assertEquals("/app/storage/images/image-of-post-1.jpg", image.get().getSource());
        assertEquals(postId, image.get().getPostId());
    }

    @Test
    void findByPostId_shouldReturnEmptyWhenImageNotFound() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                99L, "Post Without Image", "No image", 0L
        );

        Optional<Image> image = imageRepository.findByPostId(99L);

        assertTrue("Image is not empty", image.isEmpty());
    }

    @Test
    void findByPostId_shouldReturnEmptyWhenPostNotExists() {
        Optional<Image> image = imageRepository.findByPostId(999L);
        assertTrue("Image is not present in db", image.isEmpty());
    }

    @Test
    void findByPostId_shouldReturnCorrectImageForPost() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                2L, "Post 2", "Text 2", 5L
        );
        jdbcTemplate.update(
                "INSERT INTO images (id, source, post_id) VALUES (?,?,?)",
                1L, "/app/storage/images/image-of-post-1.jpg", 1L
        );
        jdbcTemplate.update(
                "INSERT INTO images (id, source, post_id) VALUES (?,?,?)",
                2L, "/app/storage/images/image-of-post-2.png", 2L
        );


        Optional<Image> image1 = imageRepository.findByPostId(1L);
        Optional<Image> image2 = imageRepository.findByPostId(2L);

        assertTrue("Image is not present in db", image1.isPresent());
        assertTrue("Image is not present in db", image2.isPresent());
        assertEquals("/app/storage/images/image-of-post-1.jpg", image1.get().getSource());
        assertEquals("/app/storage/images/image-of-post-2.png", image2.get().getSource());
    }

    @Test
    void save_multipleImagesForDifferentPosts() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Image image1 = new Image();
        image1.setSource("/app/storage/images/post-1-image.jpg");
        image1.setPostId(1L);

        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                2L, "Post 2", "Text 2", 5L
        );

        Image image2 = new Image();
        image2.setSource("/app/storage/images/post-2-image.jpg");
        image2.setPostId(2L);

        Image saved1 = imageRepository.save(image1);
        Image saved2 = imageRepository.save(image2);

        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());

        Optional<Image> retrieved1 = imageRepository.findByPostId(1L);
        Optional<Image> retrieved2 = imageRepository.findByPostId(2L);

        assertTrue("Image is not present in db", retrieved1.isPresent());
        assertTrue("Image is not present in db", retrieved2.isPresent());
        assertEquals("/app/storage/images/post-1-image.jpg", retrieved1.get().getSource());
        assertEquals("/app/storage/images/post-2-image.jpg", retrieved2.get().getSource());
    }

    @ParameterizedTest
    @CsvSource({
            ".jpg, 101",
            ".png, 102",
            ".gif, 103",
            ".webp, 104"
    })
    void findByPostId_withDifferentImageFormats(String format, Long postId) {
        String imagePath = "/app/storage/images/image" + format;

        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                postId, "Post " + postId, "Text", 0L
        );

        Image image = new Image();
        image.setSource(imagePath);
        image.setPostId(postId);
        imageRepository.save(image);
        Optional<Image> retrieved = imageRepository.findByPostId(postId);
        assertTrue("Image is not present in db", retrieved.isPresent());
        assertEquals(imagePath, retrieved.get().getSource());
    }
}

