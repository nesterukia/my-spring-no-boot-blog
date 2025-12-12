package com.nesterukia.blog.dto.post;

import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.model.Tag;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record PostDto(
        Long id,
        String title,
        String text,
        List<String> tags,
        Long likesCount,
        Long commentsCount
) {
    public static PostDto fromPost(Post post) {

        List<String> tags = post.getTags() == null ?
                List.of() : post.getTags().stream().map(Tag::getTitle).sorted().toList();

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
