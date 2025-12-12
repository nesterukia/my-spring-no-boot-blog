package com.nesterukia.blog.repository.implementations.jdbcNative;

import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

@Repository
public class JdbcNativePostRepository extends JdbcNativeRepository implements PostRepository {

    @Autowired
    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Post save(Post post) {
        String savePostSql = "INSERT INTO posts (title, text, likes_count) VALUES(?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    savePostSql,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            ps.setLong(3, post.getLikesCount() != null ? post.getLikesCount() : 0);
            return ps;
        }, keyHolder);

        long generatedPostId = ((Number) keyHolder.getKeys().get("id")).longValue();
        post.setId(generatedPostId);
        post.setComments(new HashSet<>());
        post.setTags(new HashSet<>());
        return post;
    }

    @Override
    public Post update(Long postId, Post post) {
        String updateSql = "UPDATE posts " +
                           "SET title = ?, text = ? " +
                           "WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(
                updateSql,
                post.getTitle(),
                post.getText(),
                postId
        );

        if (rowsAffected == 0) {
            throw new EntityNotFoundException(
                    String.format("Post with id = '%d' was not found.", postId)
            );
        }

        post.setId(postId);
        return post;
    }


    @Override
    public void deleteById(Long postId) {
        int rowsAffected = jdbcTemplate.update("DELETE FROM posts WHERE id = ?", postId);

        if (rowsAffected == 0) {
            throw new EntityNotFoundException(
                    String.format("Post with id = '%d' was not found", postId)
            );
        }
    }

    @Override
    public Page<Post> findAllByTextContainsIgnoreCase(String searchString, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM posts WHERE LOWER(text) LIKE LOWER(?);";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, "%" + searchString + "%");

        String sql = "SELECT id, title, text, likes_count " +
                     "FROM posts " +
                     "WHERE LOWER(text) LIKE LOWER(?) " +
                     "ORDER BY id " +
                     "LIMIT ? " +
                     "OFFSET ?;";
        List<Post> posts = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Post.class),
                "%" + searchString + "%",
                pageable.getPageSize(),
                pageable.getOffset()
        );
        return new PageImpl<>(posts, pageable, total);
    }

    @Override
    public Optional<Post> findById(Long postId) {
        String sql = "SELECT id, title, text, likes_count FROM posts WHERE id = ?";
        List<Post> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Post.class), postId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public Integer incrementAndGetLikesCount(Long postId) {
        String updateSql = "UPDATE posts " +
                           "SET likes_count = likes_count + 1 " +
                           "WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(updateSql, postId);

        if (rowsAffected == 0) {
            throw new EntityNotFoundException(
                    String.format("Post with id = '%d' was not found", postId)
            );
        }

        String selectSql = "SELECT likes_count FROM posts WHERE id = ?";
        return jdbcTemplate.queryForObject(selectSql, Integer.class, postId);
    }
}
