package com.nesterukia.blog.integration.dao;

import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.integration.config.TestDataSourceConfiguration;
import com.nesterukia.blog.model.Comment;
import com.nesterukia.blog.repository.CommentRepository;
import com.nesterukia.blog.repository.implementations.jdbcNative.JdbcNativeCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {
        TestDataSourceConfiguration.class,
        JdbcNativeCommentRepository.class
})
public class CommentRepositoryTest extends BaseDaoIntegrationTest{

    @Autowired
    private CommentRepository commentRepository;

    private Long postId;

    @BeforeEach
    public void setUp() {
        super.setUp();

        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                2L, "Post 2", "Text 2", 5L
        );

        postId = 1L;

        jdbcTemplate.update(
                "INSERT INTO comments (id, text, post_id) VALUES (?,?,?)",
                1L, "Comment 1", 1L
        );
        jdbcTemplate.update(
                "INSERT INTO comments (id, text, post_id) VALUES (?,?,?)",
                2L, "Comment 2", 1L
        );
        jdbcTemplate.update(
                "INSERT INTO comments (id, text, post_id) VALUES (?,?,?)",
                3L, "Comment 3", 2L
        );
    }

    @Test
    void save_shouldAddCommentToDatabase() {
        Comment comment = new Comment();
        comment.setText("New comment");
        comment.setPostId(postId);

        Comment saved = commentRepository.save(comment);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("New comment", saved.getText());
        assertEquals(postId, saved.getPostId());

        Set<Comment> allComments = commentRepository.findAllByPostId(postId);
        assertTrue(allComments.stream().anyMatch(c -> c.getText().equals("New comment")));
    }

    @Test
    void findAllByPostId_shouldReturnAllCommentsForPost() {
        Set<Comment> comments = commentRepository.findAllByPostId(postId);

        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertTrue(comments.stream().anyMatch(c -> c.getText().equals("Comment 1")));
        assertTrue(comments.stream().anyMatch(c -> c.getText().equals("Comment 2")));
    }

    @Test
    void findAllByPostId_shouldReturnEmptySetWhenNoComments() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                99L, "Empty Post", "No comments", 0L
        );

        Set<Comment> comments = commentRepository.findAllByPostId(99L);

        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    @Test
    void findById_shouldReturnCommentWhenExists() {
        Optional<Comment> comment = commentRepository.findById(postId, 1L);

        assertTrue(comment.isPresent());
        assertEquals(1L, comment.get().getId());
        assertEquals("Comment 1", comment.get().getText());
        assertEquals(postId, comment.get().getPostId());
    }

    @Test
    void findById_shouldReturnEmptyWhenCommentNotFound() {
        Optional<Comment> comment = commentRepository.findById(postId, 999L);
        assertTrue(comment.isEmpty());
    }

    @Test
    void findById_shouldReturnEmptyWhenWrongPostId() {
        Optional<Comment> comment = commentRepository.findById(2L, 1L);
        assertTrue(comment.isEmpty());
    }

    @Test
    void update_shouldModifyCommentText() {
        Comment updated = commentRepository.update(postId, 1L, "Updated comment text");

        assertNotNull(updated);
        assertEquals(1L, updated.getId());
        assertEquals("Updated comment text", updated.getText());
        assertEquals(postId, updated.getPostId());

        Optional<Comment> fromDb = commentRepository.findById(postId, 1L);
        assertTrue(fromDb.isPresent());
        assertEquals("Updated comment text", fromDb.get().getText());
    }

    @Test
    void update_shouldThrowExceptionWhenCommentNotFound() {
        assertThrows(EntityNotFoundException.class,
                () -> commentRepository.update(postId, 999L, "Updated text"));
    }

    @Test
    void update_shouldThrowExceptionWhenWrongPostId() {
        assertThrows(EntityNotFoundException.class,
                () -> commentRepository.update(2L, 1L, "Updated text"));
    }

    @Test
    void delete_shouldRemoveCommentFromDatabase() {
        commentRepository.delete(postId, 1L);

        Optional<Comment> deleted = commentRepository.findById(postId, 1L);
        assertTrue(deleted.isEmpty());

        Set<Comment> remaining = commentRepository.findAllByPostId(postId);
        assertEquals(1, remaining.size());
        assertTrue(remaining.stream().noneMatch(c -> c.getId().equals(1L)));
    }

    @Test
    void delete_shouldThrowExceptionWhenCommentNotFound() {
        assertThrows(EntityNotFoundException.class,
                () -> commentRepository.delete(postId, 999L));
    }

    @Test
    void delete_shouldThrowExceptionWhenWrongPostId() {
        assertThrows(EntityNotFoundException.class,
                () -> commentRepository.delete(2L, 1L));
    }

    @Test
    void deleteAllByPostId_shouldRemoveAllCommentsForPost() {
        commentRepository.deleteAllByPostId(postId);

        Set<Comment> remaining = commentRepository.findAllByPostId(postId);
        assertTrue(remaining.isEmpty());

        Set<Comment> otherPostComments = commentRepository.findAllByPostId(2L);
        assertEquals(1, otherPostComments.size());
    }

    @Test
    void deleteAllByPostId_shouldHandleNonExistingPost() {
        assertDoesNotThrow(() -> commentRepository.deleteAllByPostId(999L));
    }

    @Test
    void multipleOperations_sequentialSaveUpdateDelete() {
        Comment comment = new Comment();
        comment.setText("Initial text");
        comment.setPostId(postId);
        Comment saved = commentRepository.save(comment);
        Long commentId = saved.getId();

        assertNotNull(commentId);
        assertEquals("Initial text", saved.getText());

        Comment updated = commentRepository.update(postId, commentId, "Modified text");
        assertEquals("Modified text", updated.getText());

        Optional<Comment> verified = commentRepository.findById(postId, commentId);
        assertTrue(verified.isPresent());
        assertEquals("Modified text", verified.get().getText());

        commentRepository.delete(postId, commentId);
        Optional<Comment> deleted = commentRepository.findById(postId, commentId);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void commentsAreIsolatedByPost() {
        Set<Comment> post1Comments = commentRepository.findAllByPostId(1L);
        Set<Comment> post2Comments = commentRepository.findAllByPostId(2L);

        assertEquals(2, post1Comments.size());
        assertEquals(1, post2Comments.size());

        Set<Long> post1Ids = post1Comments.stream().map(Comment::getId).collect(Collectors.toSet());
        Set<Long> post2Ids = post2Comments.stream().map(Comment::getId).collect(Collectors.toSet());

        assertTrue(post1Ids.stream().noneMatch(post2Ids::contains));
    }
}

