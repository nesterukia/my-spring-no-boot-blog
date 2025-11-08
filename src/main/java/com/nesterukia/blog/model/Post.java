package com.nesterukia.blog.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;

@Getter
@Setter
public class Post {
    Long id;
    String title;
    String text;
    HashSet<String> tags;
    Long likesCount;
    Long commentsCount;
}