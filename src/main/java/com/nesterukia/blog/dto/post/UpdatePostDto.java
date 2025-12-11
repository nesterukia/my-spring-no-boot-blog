package com.nesterukia.blog.dto.post;

import java.util.Set;

public record UpdatePostDto(
        Long id,
        String title,
        String text,
        Set<String> tags
) {}
