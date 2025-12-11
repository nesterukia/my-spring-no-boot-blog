package com.nesterukia.blog.repository;

import com.nesterukia.blog.model.Comment;

import java.util.Optional;
import java.util.Set;

public interface CommentRepository {
    Comment save(Comment comment);
    Comment update(Long postId, Long commentId, String updatedText);
    void delete(Long postId, Long commentId);
    Set<Comment> saveComments(Long postId, Set<Comment> comment);
    Set<Comment> findAllByPostId(Long postId);
    Optional<Comment> findById(Long postId, Long commentId);
    void deleteAllByPostId(Long postId);
}
