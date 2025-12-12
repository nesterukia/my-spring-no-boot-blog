package com.nesterukia.blog.repository.implementations.jdbcNative;

import com.nesterukia.blog.model.Image;
import com.nesterukia.blog.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
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
        String saveImageSql = "INSERT INTO images (source, post_id) VALUES(?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    saveImageSql,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, image.getSource());
            ps.setLong(2, image.getPostId());
            return ps;
        }, keyHolder);

        long generatedImageId = ((Number) keyHolder.getKeys().get("id")).longValue();
        image.setId(generatedImageId);
        return image;
    }
}
