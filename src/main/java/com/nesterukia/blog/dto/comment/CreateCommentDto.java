package com.nesterukia.blog.dto.comment;

public record CreateCommentDto(
        String text,
        Long postId
) {}
