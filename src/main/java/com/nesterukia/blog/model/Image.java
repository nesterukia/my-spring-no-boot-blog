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
    Long postId;

    public Image(String source, Long postId) {
        this.source = source;
        this.postId = postId;
    }
}
