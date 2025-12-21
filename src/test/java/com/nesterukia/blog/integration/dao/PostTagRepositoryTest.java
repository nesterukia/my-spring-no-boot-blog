package com.nesterukia.blog.integration.dao;

import com.nesterukia.blog.model.Tag;
import com.nesterukia.blog.repository.PostTagRepository;
import com.nesterukia.blog.repository.implementations.jdbcNative.JdbcNativePostTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostTagRepositoryTest extends BaseDaoIntegrationTest {
    @Autowired
    private PostTagRepository postTagRepository;

    @Test
    void batchInsertPostTags_shouldInsertMultipleTags() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                1L, "tag1"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                2L, "tag2"
        );

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setTitle("tag1");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setTitle("tag2");

        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);
        tags.add(tag2);

        postTagRepository.batchInsertPostTags(1L, tags);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                1L
        );

        assertEquals(2, count);
    }

    @Test
    void batchInsertPostTags_shouldInsertCorrectRelationships() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                1L, "Java"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                2L, "Spring"
        );

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setTitle("Java");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setTitle("Spring");

        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);
        tags.add(tag2);

        postTagRepository.batchInsertPostTags(1L, tags);

        Integer javaCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ? AND tag_id = ?",
                Integer.class,
                1L, 1L
        );
        Integer springCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ? AND tag_id = ?",
                Integer.class,
                1L, 2L
        );

        assertEquals(1, javaCount);
        assertEquals(1, springCount);
    }

    @Test
    void batchInsertPostTags_withSingleTag() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                1L, "SingleTag"
        );

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setTitle("SingleTag");

        Set<Tag> tags = new HashSet<>();
        tags.add(tag);

        postTagRepository.batchInsertPostTags(1L, tags);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                1L
        );

        assertEquals(1, count);
    }

    @Test
    void batchInsertPostTags_withMultipleTagsForMultiplePosts() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                2L, "Post 2", "Text 2", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                1L, "tag1"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                2L, "tag2"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                3L, "tag3"
        );

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setTitle("tag1");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setTitle("tag2");

        Tag tag3 = new Tag();
        tag3.setId(3L);
        tag3.setTitle("tag3");

        Set<Tag> tagsForPost1 = new HashSet<>();
        tagsForPost1.add(tag1);
        tagsForPost1.add(tag2);

        Set<Tag> tagsForPost2 = new HashSet<>();
        tagsForPost2.add(tag2);
        tagsForPost2.add(tag3);

        postTagRepository.batchInsertPostTags(1L, tagsForPost1);
        postTagRepository.batchInsertPostTags(2L, tagsForPost2);

        Integer post1Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                1L
        );
        Integer post2Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                2L
        );

        assertEquals(2, post1Count);
        assertEquals(2, post2Count);
    }

    @Test
    void deleteByPostId_shouldRemoveAllTagsForPost() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                1L, "tag1"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                2L, "tag2"
        );
        jdbcTemplate.update(
                "INSERT INTO post_tags (post_id, tag_id) VALUES (?,?)",
                1L, 1L
        );
        jdbcTemplate.update(
                "INSERT INTO post_tags (post_id, tag_id) VALUES (?,?)",
                1L, 2L
        );

        postTagRepository.deleteByPostId(1L);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                1L
        );

        assertEquals(0, count);
    }

    @Test
    void deleteByPostId_shouldNotAffectOtherPosts() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                2L, "Post 2", "Text 2", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                1L, "tag1"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                2L, "tag2"
        );
        jdbcTemplate.update(
                "INSERT INTO post_tags (post_id, tag_id) VALUES (?,?)",
                1L, 1L
        );
        jdbcTemplate.update(
                "INSERT INTO post_tags (post_id, tag_id) VALUES (?,?)",
                1L, 2L
        );
        jdbcTemplate.update(
                "INSERT INTO post_tags (post_id, tag_id) VALUES (?,?)",
                2L, 1L
        );

        postTagRepository.deleteByPostId(1L);

        Integer post1Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                1L
        );
        Integer post2Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                2L
        );

        assertEquals(0, post1Count);
        assertEquals(1, post2Count);
    }

    @Test
    void deleteByPostId_nonExistingPost() {
        assertDoesNotThrow(() -> postTagRepository.deleteByPostId(999L));
    }

    @Test
    void batchInsertPostTags_withLargeSetOfTags() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Set<Tag> tags = new HashSet<>();
        for (int i = 1; i <= 50; i++) {
            jdbcTemplate.update(
                    "INSERT INTO tags (id, title) VALUES (?,?)",
                    (long) i, "tag" + i
            );
            Tag tag = new Tag();
            tag.setId((long) i);
            tag.setTitle("tag" + i);
            tags.add(tag);
        }

        postTagRepository.batchInsertPostTags(1L, tags);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                1L
        );

        assertEquals(50, count);
    }

    @Test
    void batchInsertPostTags_emptySet() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Set<Tag> emptyTags = new HashSet<>();

        assertDoesNotThrow(() -> postTagRepository.batchInsertPostTags(1L, emptyTags));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                1L
        );

        assertEquals(0, count);
    }

    @Test
    void deleteByPostId_thenBatchInsertNewTags() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                1L, "tag1"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                2L, "tag2"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                3L, "tag3"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                4L, "tag4"
        );
        jdbcTemplate.update(
                "INSERT INTO post_tags (post_id, tag_id) VALUES (?,?)",
                1L, 1L
        );
        jdbcTemplate.update(
                "INSERT INTO post_tags (post_id, tag_id) VALUES (?,?)",
                1L, 2L
        );

        postTagRepository.deleteByPostId(1L);

        Tag tag3 = new Tag();
        tag3.setId(3L);
        tag3.setTitle("tag3");

        Tag tag4 = new Tag();
        tag4.setId(4L);
        tag4.setTitle("tag4");

        Set<Tag> newTags = new HashSet<>();
        newTags.add(tag3);
        newTags.add(tag4);

        postTagRepository.batchInsertPostTags(1L, newTags);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                1L
        );

        Integer tag3Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ? AND tag_id = ?",
                Integer.class,
                1L, 3L
        );

        assertEquals(2, count);
        assertEquals(1, tag3Count);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 50})
    void batchInsertPostTags_withVaryingTagCounts(int tagCount) {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Set<Tag> tags = new HashSet<>();
        for (int i = 1; i <= tagCount; i++) {
            jdbcTemplate.update(
                    "INSERT INTO tags (id, title) VALUES (?,?)",
                    (long) i, "tag" + i
            );
            Tag tag = new Tag();
            tag.setId((long) i);
            tag.setTitle("tag" + i);
            tags.add(tag);
        }

        postTagRepository.batchInsertPostTags(1L, tags);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tags WHERE post_id = ?",
                Integer.class,
                1L
        );

        assertEquals(tagCount, count);
    }
}