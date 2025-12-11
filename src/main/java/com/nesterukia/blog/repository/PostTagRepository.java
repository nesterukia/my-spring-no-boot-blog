package com.nesterukia.blog.repository;

import com.nesterukia.blog.model.Tag;

import java.util.Set;

public interface PostTagRepository {
    void batchInsertPostTags(Long postId, Set<Tag> tags);
    void deleteByPostId(Long postId);
}
