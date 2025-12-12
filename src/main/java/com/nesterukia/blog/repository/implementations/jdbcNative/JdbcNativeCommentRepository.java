package com.nesterukia.blog.repository.implementations.jdbcNative;

import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.model.Comment;
import com.nesterukia.blog.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        String updateSql = """
            UPDATE comments
            SET text = ?
            WHERE post_id = ? AND id = ?
        """;

        int rowsAffected = jdbcTemplate.update(updateSql, updatedText, postId, commentId);

        if (rowsAffected == 0) {
            throw new EntityNotFoundException(
                    String.format("Comment not found with id: %d for post: %d", commentId, postId)
            );
        }

        String selectSql = "SELECT id, text, post_id FROM comments WHERE id = ? AND post_id = ?";
        return jdbcTemplate.queryForObject(
                selectSql,
                new BeanPropertyRowMapper<>(Comment.class),
                commentId,
                postId
        );
    }


    @Override
    public void delete(Long postId, Long commentId) {
        int rowsAffected = jdbcTemplate.update("DELETE FROM comments WHERE post_id = ? AND id = ? ", postId, commentId);

        if (rowsAffected == 0) {
            throw new EntityNotFoundException(
                    String.format("Comment with id = '%d' for post with id = '%d' was not found", commentId, postId)
            );
        }
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
        String insertSql = "INSERT INTO comments(id, text, post_id) VALUES (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    insertSql,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, nextId);
            ps.setString(2, text);
            ps.setLong(3, postId);
            return ps;
        }, keyHolder);

        return new Comment(nextId, text, postId);
    }


    private Long getNextCommentIdForPost(Long postId) {
        Long maxId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) FROM comments WHERE post_id = ?",
                Long.class, postId
        );
        return maxId + 1;
    }
}
