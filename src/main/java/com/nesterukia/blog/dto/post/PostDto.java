package com.nesterukia.blog.dto.post;

import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.model.Tag;

import java.util.Set;
import java.util.stream.Collectors;

public record PostDto(
        Long id,
        String title,
        String text,
        Set<String> tags,
        Long likesCount,
        Long commentsCount
) {
    public static PostDto fromPost(Post post) {

        Set<String> tags = post.getTags() == null ?
                Set.of() : post.getTags().stream().map(Tag::getTitle).collect(Collectors.toSet());

        Long commentsCount = post.getComments() == null ?
                0L : Long.valueOf(post.getComments().size());
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                tags,
                post.getLikesCount(),
                commentsCount
        );
    }
}
