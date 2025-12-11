package com.nesterukia.blog.dto.post;

import java.util.List;

public record PostPageResponse(
        List<PostDto> posts,
        Boolean hasPrev,
        Boolean hasNext,
        Integer lastPage
) {}
