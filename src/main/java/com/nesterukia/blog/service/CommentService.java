package com.nesterukia.blog.service;

import com.nesterukia.blog.dto.comment.CreateCommentDto;
import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.model.Comment;
import com.nesterukia.blog.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CommentService {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment save(CreateCommentDto createCommentDto) {
        createCommentDto.validateMandatoryFields();
        return commentRepository.save(Comment.fromCreateCommentDto(createCommentDto));
    }

    public Comment updateComment(Long postId, Long commentId, String updatedText) {
        return commentRepository.update(postId, commentId, updatedText);
    }

    public void delete(Long postId, Long commentId) {
        commentRepository.delete(postId, commentId);
    }

    public Set<Comment> findAllByPostId(Long postId) {
        return commentRepository.findAllByPostId(postId);
    }

    public Comment findCommentById(Long postId, Long commentId) {
        return commentRepository.findById(postId, commentId).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("Comment with id = '%s' for postId = '%s' was not found.", commentId, postId)
                )
        );
    }
}
