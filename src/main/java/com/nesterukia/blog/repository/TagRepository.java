package com.nesterukia.blog.repository;

import com.nesterukia.blog.model.Tag;

import java.util.Set;

public interface TagRepository {
    Tag save(String title);
    Set<Tag> saveTags(Long postId, Set<String> tags);
    Tag findTagByTitle(String title);
    Set<Tag> findTagsByPostId(Long postId);
}
