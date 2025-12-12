package com.nesterukia.blog.repository;

import com.nesterukia.blog.model.Image;

import java.util.Optional;

public interface ImageRepository {
    Optional<Image> findByPostId(Long postId);
    Image save(Image image);
}
