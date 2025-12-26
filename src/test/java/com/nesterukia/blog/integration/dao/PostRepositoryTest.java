package com.nesterukia.blog.integration.dao;

import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertTrue;

public class PostRepositoryTest extends BaseDaoIntegrationTest {
    @Autowired
    private PostRepository postRepository;

    @Test
    void save_shouldAddPostToDatabase() {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setText("Test text content");
        post.setLikesCount(0L);

        Post saved = postRepository.save(post);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Test Post", saved.getTitle());
        assertEquals("Test text content", saved.getText());
        assertEquals(0L, saved.getLikesCount());

        Optional<Post> fromDb = postRepository.findById(saved.getId());
        assertTrue("Post is not present in db", fromDb.isPresent());
        assertEquals("Test Post", fromDb.get().getTitle());
    }

    @Test
    void save_shouldGenerateAutoIncrementId() {
        Post post1 = new Post();
        post1.setTitle("Post 1");
        post1.setText("Text 1");
        post1.setLikesCount(0L);

        Post post2 = new Post();
        post2.setTitle("Post 2");
        post2.setText("Text 2");
        post2.setLikesCount(0L);

        Post saved1 = postRepository.save(post1);
        Post saved2 = postRepository.save(post2);

        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotEquals(saved1.getId(), saved2.getId());
    }

    @Test
    void findById_shouldReturnPostWhenExists() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Test Post", "Test text", 5L
        );

        Optional<Post> post = postRepository.findById(1L);

        assertTrue("Post is not present in db", post.isPresent());
        assertEquals("Test Post", post.get().getTitle());
        assertEquals("Test text", post.get().getText());
        assertEquals(5L, post.get().getLikesCount());
    }

    @Test
    void findById_shouldReturnEmptyWhenPostNotFound() {
        Optional<Post> post = postRepository.findById(999L);

        assertTrue("Post is not empty", post.isEmpty());
    }

    @Test
    void update_shouldModifyPostFields() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Original Title", "Original text", 0L
        );

        Post postToUpdate = new Post();
        postToUpdate.setTitle("Updated Title");
        postToUpdate.setText("Updated text");

        Post updated = postRepository.update(1L, postToUpdate);

        assertNotNull(updated);
        assertEquals(1L, updated.getId());
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated text", updated.getText());

        Optional<Post> fromDb = postRepository.findById(1L);
        assertTrue("Post is not present in db", fromDb.isPresent());
        assertEquals("Updated Title", fromDb.get().getTitle());
        assertEquals("Updated text", fromDb.get().getText());
    }

    @Test
    void update_shouldThrowExceptionWhenPostNotFound() {
        Post postToUpdate = new Post();
        postToUpdate.setTitle("Updated Title");
        postToUpdate.setText("Updated text");

        assertThrows(EntityNotFoundException.class, () -> postRepository.update(999L, postToUpdate));
    }

    @Test
    void deleteById_shouldRemovePostFromDatabase() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post to delete", "Text", 0L
        );

        postRepository.deleteById(1L);

        Optional<Post> deleted = postRepository.findById(1L);
        assertTrue("Post is not empty", deleted.isEmpty());
    }

    @Test
    void deleteById_shouldThrowExceptionWhenPostNotFound() {
        assertThrows(EntityNotFoundException.class, () -> postRepository.deleteById(999L));
    }

    @Test
    void incrementAndGetLikesCount_shouldIncrementLikes() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post", "Text", 5L
        );

        Integer likesCount = postRepository.incrementAndGetLikesCount(1L);

        assertEquals(6, likesCount);

        Optional<Post> fromDb = postRepository.findById(1L);
        assertTrue("Post is not present in db", fromDb.isPresent());
        assertEquals(6L, fromDb.get().getLikesCount());
    }

    @Test
    void incrementAndGetLikesCount_shouldIncrementFromZero() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post", "Text", 0L
        );

        Integer likesCount = postRepository.incrementAndGetLikesCount(1L);

        assertEquals(1, likesCount);
    }

    @Test
    void incrementAndGetLikesCount_multipleIncrements() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post", "Text", 0L
        );

        Integer likes1 = postRepository.incrementAndGetLikesCount(1L);
        Integer likes2 = postRepository.incrementAndGetLikesCount(1L);
        Integer likes3 = postRepository.incrementAndGetLikesCount(1L);

        assertEquals(1, likes1);
        assertEquals(2, likes2);
        assertEquals(3, likes3);
    }

    @Test
    void incrementAndGetLikesCount_shouldThrowExceptionWhenPostNotFound() {
        assertThrows(EntityNotFoundException.class, () -> postRepository.incrementAndGetLikesCount(999L));
    }

    @Test
    void findAllByTextContainsIgnoreCase_shouldReturnMatchingPosts() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "This is about Spring Framework", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                2L, "Post 2", "This is about Java programming", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                3L, "Post 3", "This is about Spring Boot", 0L
        );

        Page<Post> result = postRepository.findAllByTextContainsIgnoreCase("Spring", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertTrue("Posts not found", result.getContent().stream().anyMatch(p -> p.getText().contains("Spring Framework")));
        assertTrue("Posts not found", result.getContent().stream().anyMatch(p -> p.getText().contains("Spring Boot")));
    }

    @Test
    void findAllByTextContainsIgnoreCase_shouldBeCaseInsensitive() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Learning JAVA programming", 0L
        );

        Page<Post> resultLower = postRepository.findAllByTextContainsIgnoreCase("java", PageRequest.of(0, 10));
        Page<Post> resultUpper = postRepository.findAllByTextContainsIgnoreCase("JAVA", PageRequest.of(0, 10));
        Page<Post> resultMixed = postRepository.findAllByTextContainsIgnoreCase("Java", PageRequest.of(0, 10));

        assertEquals(1, resultLower.getTotalElements());
        assertEquals(1, resultUpper.getTotalElements());
        assertEquals(1, resultMixed.getTotalElements());
    }

    @Test
    void findAllByTextContainsIgnoreCase_shouldReturnEmptyWhenNoMatch() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "About Java programming", 0L
        );

        Page<Post> result = postRepository.findAllByTextContainsIgnoreCase("Python", PageRequest.of(0, 10));

        assertTrue("Posts not empty", result.isEmpty());
    }

    @Test
    void findAllByTextContainsIgnoreCase_shouldSupportPagination() {
        for (int i = 1; i <= 25; i++) {
            jdbcTemplate.update(
                    "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                    (long) i, "Post " + i, "Spring content number " + i, 0L
            );
        }

        Page<Post> page1 = postRepository.findAllByTextContainsIgnoreCase("Spring", PageRequest.of(0, 10));
        Page<Post> page2 = postRepository.findAllByTextContainsIgnoreCase("Spring", PageRequest.of(1, 10));
        Page<Post> page3 = postRepository.findAllByTextContainsIgnoreCase("Spring", PageRequest.of(2, 10));

        assertEquals(25, page1.getTotalElements());
        assertEquals(10, page1.getContent().size());
        assertEquals(10, page2.getContent().size());
        assertEquals(5, page3.getContent().size());
        assertEquals(3, page1.getTotalPages());
    }

    @Test
    void findAllByTextContainsIgnoreCase_shouldSearchInFullText() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post", "The quick brown fox jumps over the lazy dog", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                2L, "Post", "The lazy dog sleeps all day", 0L
        );

        Page<Post> result = postRepository.findAllByTextContainsIgnoreCase("lazy", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void save_multiplePosts() {
        Post post1 = new Post();
        post1.setTitle("First Post");
        post1.setText("First text");
        post1.setLikesCount(0L);

        Post post2 = new Post();
        post2.setTitle("Second Post");
        post2.setText("Second text");
        post2.setLikesCount(0L);

        Post post3 = new Post();
        post3.setTitle("Third Post");
        post3.setText("Third text");
        post3.setLikesCount(0L);

        Post saved1 = postRepository.save(post1);
        Post saved2 = postRepository.save(post2);
        Post saved3 = postRepository.save(post3);

        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotNull(saved3.getId());

        Optional<Post> retrieved1 = postRepository.findById(saved1.getId());
        Optional<Post> retrieved2 = postRepository.findById(saved2.getId());
        Optional<Post> retrieved3 = postRepository.findById(saved3.getId());

        assertTrue("Posts not present in db", retrieved1.isPresent());
        assertTrue("Posts not present in db", retrieved2.isPresent());
        assertTrue("Posts not present in db", retrieved3.isPresent());
    }

    @ParameterizedTest
    @CsvSource({
            "Post Title One,Post text content one",
            "Post Title Two,Post text content two with more details",
            "Very Long Post Title That Contains Many Words,Very long post text content",
            "简体中文标题,中文文本内容"
    })
    void save_withDifferentTextContent(String title, String text) {
        Post post = new Post();
        post.setTitle(title);
        post.setText(text);
        post.setLikesCount(0L);

        Post saved = postRepository.save(post);

        assertNotNull(saved.getId());
        assertEquals(title, saved.getTitle());
        assertEquals(text, saved.getText());

        Optional<Post> fromDb = postRepository.findById(saved.getId());
        assertTrue("Post is not present in db", fromDb.isPresent());
        assertEquals(title, fromDb.get().getTitle());
        assertEquals(text, fromDb.get().getText());
    }
}
