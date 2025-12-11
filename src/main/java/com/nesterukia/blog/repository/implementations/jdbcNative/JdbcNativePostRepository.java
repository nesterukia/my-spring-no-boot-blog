package com.nesterukia.blog.repository.implementations.jdbcNative;

import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
        String savePostSql = """
            INSERT INTO posts
            (title, "text", likes_count)
            VALUES(?, ?, ?)
            RETURNING id, title, "text", likes_count
        """;

        return jdbcTemplate.queryForObject(savePostSql,
                new BeanPropertyRowMapper<>(Post.class),
                post.getTitle(),
                post.getText(),
                post.getLikesCount() != null ? post.getLikesCount() : 0
        );
    }

    @Override
    public void deleteById(Long postId) {
        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", postId);
    }

    @Override
    public Page<Post> findAllByTextContainsIgnoreCase(String searchString, Pageable pageable) {
        String countSql = """
               SELECT COUNT(*)
               FROM posts
               WHERE LOWER(text) LIKE LOWER(?);
        """;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, "%" + searchString + "%");

        String sql = """
                SELECT id, title, text, likes_count
                FROM posts
                WHERE LOWER(text) LIKE LOWER(?)
                ORDER BY id
                LIMIT ?
                OFFSET ?;
        """;
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
        String sql = """
            UPDATE posts
            SET likes_count = likes_count + 1
            WHERE id = ?
            RETURNING likes_count
        """;

        return jdbcTemplate.queryForObject(sql, Integer.class, postId);
    }

}
