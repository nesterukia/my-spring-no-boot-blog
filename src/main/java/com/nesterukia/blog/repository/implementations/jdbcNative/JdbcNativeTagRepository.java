package com.nesterukia.blog.repository.implementations.jdbcNative;

import com.nesterukia.blog.model.Tag;
import com.nesterukia.blog.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class JdbcNativeTagRepository extends JdbcNativeRepository implements TagRepository {

    @Autowired
    public JdbcNativeTagRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Tag save(String title) {
        String saveSql = "INSERT INTO tags(title) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    saveSql,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, title);
            return ps;
        }, keyHolder);

        long tagId = ((Number) keyHolder.getKeys().get("id")).longValue();
        return new Tag(tagId, title);
    }


    @Override
    public Set<Tag> saveTags(Long postId, Set<String> tagTitles) {
        List<Tag> tags = tagTitles.stream().map(this::getOrCreateTagByTitle).toList();
        return new HashSet<>(tags);
    }

    @Override
    public Tag findTagByTitle(String title) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id, title FROM tags WHERE title = ?",
                    new BeanPropertyRowMapper<>(Tag.class),
                    title
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Set<Tag> findTagsByPostId(Long postId) {
        String findTagsQuery = "SELECT t.id, t.title " +
                               "FROM tags t " +
                               "JOIN post_tags pt ON t.id = pt.tag_id " +
                               "WHERE pt.post_id = ?";

        List<Tag> tags = jdbcTemplate.query(
                findTagsQuery,
                new BeanPropertyRowMapper<>(Tag.class),
                postId
        );
        return new HashSet<>(tags);
    }

    private Tag getOrCreateTagByTitle(String title) {
        Tag tag = findTagByTitle(title);

        if (tag == null) {
            tag = save(title);
        }

        return tag;
    }
}
