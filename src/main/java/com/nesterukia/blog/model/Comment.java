package com.nesterukia.blog.model;

import com.nesterukia.blog.dto.comment.CreateCommentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@With
@Builder
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
