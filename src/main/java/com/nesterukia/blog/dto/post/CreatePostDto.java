package com.nesterukia.blog.dto.post;

import java.util.Set;

public record CreatePostDto(
        String title,
        String text,
        Set<String> tags
) {}
