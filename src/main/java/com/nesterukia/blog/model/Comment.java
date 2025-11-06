package com.nesterukia.blog.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comment {
    Long id;
    String text;
    Long postId;
}
