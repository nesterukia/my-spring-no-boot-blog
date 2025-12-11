package com.nesterukia.blog.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    Long id;
    String source;
    Post post;

    public Image(String source, Post post) {
        this.source = source;
        this.post = post;
    }
}
