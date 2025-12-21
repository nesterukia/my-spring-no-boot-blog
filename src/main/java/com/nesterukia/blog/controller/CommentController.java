package com.nesterukia.blog.controller;

import com.nesterukia.blog.dto.comment.CommentDto;
import com.nesterukia.blog.dto.comment.CreateCommentDto;
import com.nesterukia.blog.service.CommentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/posts")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{postId}/comments")
    public Set<CommentDto> getAllComments(@PathVariable(name = "postId") Long postId){
        return commentService.findAllByPostId(postId).stream()
                .map(CommentDto::fromComment)
                .collect(Collectors.toSet());
    }

    @PostMapping("/{postId}/comments")
    public CommentDto addComment(@RequestBody CreateCommentDto createCommentDto){
        return CommentDto.fromComment(commentService.save(createCommentDto));
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public CommentDto getCommentById(@PathVariable(name = "postId") Long postId,
                                     @PathVariable(name = "commentId") Long commentId) {
        return CommentDto.fromComment(commentService.findCommentById(postId, commentId));
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public CommentDto updateComment(@PathVariable(name = "postId") Long postId,
                                    @PathVariable(name = "commentId") Long commentId,
                                    @RequestBody CommentDto commentDto) {
        commentDto.validateMandatoryFields();
        return CommentDto.fromComment(commentService.updateComment(postId, commentId, commentDto.text()));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public void updateComment(@PathVariable(name = "postId") Long postId,
                              @PathVariable(name = "commentId") Long commentId) {
        commentService.delete(postId, commentId);
    }
}
