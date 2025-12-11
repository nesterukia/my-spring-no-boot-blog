package com.nesterukia.blog.dto.comment;

public record UpdateCommentDto(
        Long id,
        String text,
        Long postId
) {}
