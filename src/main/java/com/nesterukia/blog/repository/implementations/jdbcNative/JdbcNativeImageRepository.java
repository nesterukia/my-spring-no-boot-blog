package com.nesterukia.blog.repository.implementations.jdbcNative;

import com.nesterukia.blog.model.Comment;
import com.nesterukia.blog.model.Image;
import com.nesterukia.blog.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcNativeImageRepository extends JdbcNativeRepository implements ImageRepository {

    @Autowired
    public JdbcNativeImageRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Optional<Image> findByPostId(Long postId) {
        String sql = "SELECT id, source, post_id FROM images WHERE post_id = ?";
        List<Image> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Image.class), postId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public Image save(Image image) {
        String savePostSql = """
            INSERT INTO images
            (source, post_id)
            VALUES(?, ?)
            RETURNING id, source, post_id
        """;

        return jdbcTemplate.queryForObject(savePostSql,
                new BeanPropertyRowMapper<>(Image.class),
                image.getSource(),
                image.getPost().getId()
        );
    }
}
