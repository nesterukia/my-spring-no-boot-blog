package com.nesterukia.blog.repository.implementations.jdbcNative;

import com.nesterukia.blog.model.Tag;
import com.nesterukia.blog.repository.PostTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

@Repository
public class JdbcNativePostTagRepository extends JdbcNativeRepository implements PostTagRepository {

    @Autowired
    public JdbcNativePostTagRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void batchInsertPostTags(Long postId, Set<Tag> tags) {
        String insertSql = "INSERT INTO post_tags(post_id, tag_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, postId);
                ps.setLong(2, new ArrayList<>(tags).get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return tags.size();
            }
        });
    }

    @Override
    public void deleteByPostId(Long postId) {
        jdbcTemplate.update("DELETE FROM post_tags WHERE post_id = ?", postId);
    }
}
