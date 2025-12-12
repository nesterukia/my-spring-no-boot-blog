package com.nesterukia.blog.integration.dao;

import com.nesterukia.blog.integration.config.TestDataSourceConfiguration;
import com.nesterukia.blog.model.Tag;
import com.nesterukia.blog.repository.TagRepository;
import com.nesterukia.blog.repository.implementations.jdbcNative.JdbcNativeTagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotEquals;

@SpringJUnitConfig(classes = {
        TestDataSourceConfiguration.class,
        JdbcNativeTagRepository.class
})
public class TagRepositoryTest extends BaseDaoIntegrationTest {
    @Autowired
    private TagRepository tagRepository;

    @Test
    void save_shouldAddTagToDatabase() {
        Tag saved = tagRepository.save("Java");

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Tag title is not equal to expected", "Java", saved.getTitle());

        Tag fromDb = tagRepository.findTagByTitle("Java");
        assertNotNull(fromDb);
        assertEquals("Tag title is not equal to expected", "Java", fromDb.getTitle());
    }

    @Test
    void save_shouldGenerateAutoIncrementId() {
        Tag saved1 = tagRepository.save("Java");
        Tag saved2 = tagRepository.save("Spring");
        Tag saved3 = tagRepository.save("Docker");

        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotNull(saved3.getId());
        assertNotEquals("Tag title is equal to expected", saved1.getId(), saved2.getId());
        assertNotEquals("Tag title is equal to expected", saved2.getId(), saved3.getId());
    }

    @Test
    void save_shouldPreserveTagTitle() {
        String[] titles = {"JavaScript", "Python", "C++", "Go", "Rust"};

        for (String title : titles) {
            Tag saved = tagRepository.save(title);
            assertEquals("Tag title is not equal to expected", title, saved.getTitle());

            Tag fromDb = tagRepository.findTagByTitle(title);
            assertNotNull(fromDb);
            assertEquals("Tag title is not equal to expected", title, fromDb.getTitle());
        }
    }

    @Test
    void findTagByTitle_shouldReturnTagWhenExists() {
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                1L, "Java"
        );

        Tag tag = tagRepository.findTagByTitle("Java");

        assertNotNull(tag);
        assertEquals("Tag id is not equal to expected", 1L, tag.getId());
        assertEquals("Tag title is not equal to expected", "Java", tag.getTitle());
    }

    @Test
    void findTagByTitle_shouldReturnNullWhenNotFound() {
        Tag tag = tagRepository.findTagByTitle("NonExistentTag");
        assertNull(tag);
    }

    @Test
    void findTagByTitle_shouldBeCaseSensitive() {
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                1L, "Java"
        );

        Tag exactMatch = tagRepository.findTagByTitle("Java");
        assertNotNull(exactMatch);

        Tag lowerCase = tagRepository.findTagByTitle("java");
        assertNull(lowerCase);

        Tag upperCase = tagRepository.findTagByTitle("JAVA");
        assertNull(upperCase);
    }

    @Test
    void saveTags_shouldCreateMultipleTags() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Set<String> tagTitles = new HashSet<>();
        tagTitles.add("Java");
        tagTitles.add("Spring");
        tagTitles.add("Docker");

        Set<Tag> savedTags = tagRepository.saveTags(1L, tagTitles);

        assertEquals("Tag array size is not equal to expected", 3, savedTags.size());
        assertTrue(savedTags.stream().anyMatch(t -> "Java".equals(t.getTitle())));
        assertTrue(savedTags.stream().anyMatch(t -> "Spring".equals(t.getTitle())));
        assertTrue(savedTags.stream().anyMatch(t -> "Docker".equals(t.getTitle())));
    }

    @Test
    void saveTags_shouldGenerateUniqueIds() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Set<String> tagTitles = new HashSet<>();
        tagTitles.add("Java");
        tagTitles.add("Spring");
        tagTitles.add("Docker");

        Set<Tag> savedTags = tagRepository.saveTags(1L, tagTitles);

        Set<Long> ids = savedTags.stream().map(Tag::getId).collect(Collectors.toSet());
        assertEquals("Tag ids array is not equal to expected", 3, ids.size());
    }

    @Test
    void saveTags_withSingleTag() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Set<String> tagTitles = new HashSet<>();
        tagTitles.add("Kubernetes");

        Set<Tag> savedTags = tagRepository.saveTags(1L, tagTitles);

        assertEquals("Tag array size is not equal to expected", 1, savedTags.size());
        assertEquals("Tag title is not equal to expected", "Kubernetes", savedTags.iterator().next().getTitle());
    }

    @Test
    void saveTags_emptySet() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Set<String> emptyTags = new HashSet<>();

        Set<Tag> savedTags = tagRepository.saveTags(1L, emptyTags);

        assertTrue(savedTags.isEmpty());
    }

    @Test
    void findTagsByPostId_shouldReturnAllTagsForPost() {
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
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                3L, "Docker"
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
                1L, 3L
        );

        Set<Tag> tags = tagRepository.findTagsByPostId(1L);

        assertEquals("Tag array size is not equal to expected",3, tags.size());
        assertTrue(tags.stream().anyMatch(t -> "Java".equals(t.getTitle())));
        assertTrue(tags.stream().anyMatch(t -> "Spring".equals(t.getTitle())));
        assertTrue(tags.stream().anyMatch(t -> "Docker".equals(t.getTitle())));
    }

    @Test
    void findTagsByPostId_shouldReturnEmptySetWhenNoTags() {
        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post 1", "Text 1", 0L
        );

        Set<Tag> tags = tagRepository.findTagsByPostId(1L);

        assertTrue(tags.isEmpty());
    }

    @Test
    void findTagsByPostId_shouldReturnEmptySetWhenPostNotExists() {
        Set<Tag> tags = tagRepository.findTagsByPostId(999L);

        assertTrue(tags.isEmpty());
    }

    @Test
    void findTagsByPostId_shouldReturnCorrectTagsForEachPost() {
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
                1L, "Java"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                2L, "Spring"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                3L, "Docker"
        );
        jdbcTemplate.update(
                "INSERT INTO tags (id, title) VALUES (?,?)",
                4L, "Kubernetes"
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
                2L, 3L
        );
        jdbcTemplate.update(
                "INSERT INTO post_tags (post_id, tag_id) VALUES (?,?)",
                2L, 4L
        );

        Set<Tag> post1Tags = tagRepository.findTagsByPostId(1L);
        Set<Tag> post2Tags = tagRepository.findTagsByPostId(2L);

        assertEquals("Tag array size is not equal to expected",2, post1Tags.size());
        assertEquals("Tag array size is not equal to expected",2, post2Tags.size());
        assertTrue(post1Tags.stream().anyMatch(t -> "Java".equals(t.getTitle())));
        assertTrue(post1Tags.stream().anyMatch(t -> "Spring".equals(t.getTitle())));
        assertTrue(post2Tags.stream().anyMatch(t -> "Docker".equals(t.getTitle())));
        assertTrue(post2Tags.stream().anyMatch(t -> "Kubernetes".equals(t.getTitle())));
    }

    @Test
    void save_multipleTagsWithDifferentTitles() {
        String[] titles = {"Backend", "Frontend", "DevOps", "Testing", "Security"};

        for (String title : titles) {
            Tag saved = tagRepository.save(title);
            assertNotNull(saved.getId());
            assertEquals("Tag title is not equal to expected", title, saved.getTitle());
        }

        Integer totalTags = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tags",
                Integer.class
        );

        assertEquals("Tag array size is not equal to expected", titles.length, totalTags);
    }

    @Test
    void findTagByTitle_withSpecialCharacters() {
        tagRepository.save("C++");
        Tag found = tagRepository.findTagByTitle("C++");
        assertNotNull(found);
        assertEquals("Tag title is not equal to expected","C++", found.getTitle());
    }
}

