package com.nesterukia.blog.repository.implementations.jdbcNative;

import com.nesterukia.blog.model.Comment;
import com.nesterukia.blog.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class JdbcNativeCommentRepository extends JdbcNativeRepository implements CommentRepository {

    @Autowired
    public JdbcNativeCommentRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Comment save(Comment comment) {
        return insertAndGetComment(comment.getPostId(), comment.getText());
    }

    @Override
    public Comment update(Long postId, Long commentId, String updatedText) {
        String sql = """
            UPDATE comments
            SET "text" = ?
            WHERE post_id = ? AND id = ?
            RETURNING id, "text", post_id
        """;
        return jdbcTemplate.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(Comment.class),
                updatedText,
                postId,
                commentId
        );
    }

    @Override
    public void delete(Long postId, Long commentId) {
        jdbcTemplate.update("DELETE FROM comments WHERE post_id = ? AND id = ? ", postId, commentId);
    }

    @Override
    public Set<Comment> saveComments(Long postId, Set<Comment> comments) {
        return comments.stream()
                .map(comment -> insertAndGetComment(postId, comment.getText()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Comment> findAllByPostId(Long postId) {
        String sql = "SELECT id, text, post_id FROM comments WHERE post_id = ?";
        List<Comment> comments = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Comment.class),
                postId
        );
        return new HashSet<>(comments);
    }

    @Override
    public Optional<Comment> findById(Long postId, Long commentId) {
        String sql = "SELECT id, text, post_id FROM comments WHERE post_id = ? AND id = ?";
        List<Comment> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Comment.class), postId, commentId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public void deleteAllByPostId(Long postId) {
        jdbcTemplate.update("DELETE FROM comments WHERE post_id = ?", postId);
    }

    private Comment insertAndGetComment(Long postId, String text) {
        Long nextId = getNextCommentIdForPost(postId);

        String sql = "INSERT INTO comments(id, text, post_id) VALUES (?, ?, ?) RETURNING id, text, post_id";
        return jdbcTemplate.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(Comment.class),
                nextId,
                text,
                postId
        );
    }

    private Long getNextCommentIdForPost(Long postId) {
        Long maxId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) FROM comments WHERE post_id = ?",
                Long.class, postId
        );
        return maxId + 1;
    }
}
