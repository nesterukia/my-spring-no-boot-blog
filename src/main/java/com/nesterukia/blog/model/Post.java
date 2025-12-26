package com.nesterukia.blog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Post {
    Long id;
    String title;
    String text;
    Long likesCount;
    Set<Tag> tags;
    Set<Comment> comments;
}