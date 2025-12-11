package com.nesterukia.blog.dto.comment;

import com.nesterukia.blog.model.Comment;

public record CommentDto(
        Long id,
        String text,
        Long postId
) {
    public static CommentDto fromComment(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getPostId()
        );
    }
}
