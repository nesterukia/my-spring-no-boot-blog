package com.nesterukia.blog.repository;


import com.nesterukia.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostRepository {
    Post save(Post post);
    void deleteById(Long postId);
    Page<Post> findAllByTextContainsIgnoreCase(String searchString, Pageable pageable);
    Optional<Post> findById(Long postId);
    Integer incrementAndGetLikesCount(Long postId);
}
