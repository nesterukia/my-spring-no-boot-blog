package com.nesterukia.blog.service;

import com.nesterukia.blog.dto.comment.CreateCommentDto;
import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.model.Comment;
import com.nesterukia.blog.repository.CommentRepository;
import com.nesterukia.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public Comment save(CreateCommentDto createCommentDto) {
        createCommentDto.validateMandatoryFields();
        validatePostExists(createCommentDto.postId());
        return commentRepository.save(Comment.fromCreateCommentDto(createCommentDto));
    }

    public Comment updateComment(Long postId, Long commentId, String updatedText) {
        validatePostExists(postId);
        return commentRepository.update(postId, commentId, updatedText);
    }

    public void delete(Long postId, Long commentId) {
        commentRepository.delete(postId, commentId);
    }

    public Set<Comment> findAllByPostId(Long postId) {
        validatePostExists(postId);
        return commentRepository.findAllByPostId(postId);
    }

    public Comment findCommentById(Long postId, Long commentId) {
        return commentRepository.findById(postId, commentId).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("Comment with id = '%s' for postId = '%s' was not found.", commentId, postId)
                )
        );
    }

    private void validatePostExists(Long postId) {
        postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("No Post found with id = " + postId)
        );
    }
}
