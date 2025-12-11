package com.nesterukia.blog.repository;


import com.nesterukia.blog.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByTextContainsIgnoreCase(String searchString, Pageable pageable);
}
