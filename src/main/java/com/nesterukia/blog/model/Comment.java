package com.nesterukia.blog.model;

import com.nesterukia.blog.dto.comment.CreateCommentDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    Long id;
    String text;
    Long postId;

    public static Comment fromCreateCommentDto(CreateCommentDto createCommentDto) {
        return new Comment(
                null,
                createCommentDto.text(),
                createCommentDto.postId()
        );
    }
}
