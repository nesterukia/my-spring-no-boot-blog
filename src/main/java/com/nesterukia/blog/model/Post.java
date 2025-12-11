package com.nesterukia.blog.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Post {
    Long id;
    String title;
    String text;
    Long likesCount;
    Set<Tag> tags;
    private Set<Comment> comments;
}