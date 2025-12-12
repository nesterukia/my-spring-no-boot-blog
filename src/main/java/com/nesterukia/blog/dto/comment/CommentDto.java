package com.nesterukia.blog.dto.comment;

import com.nesterukia.blog.exceptions.MandatoryFieldAbsentException;
import com.nesterukia.blog.model.Comment;

public record CommentDto(
        Long id,
        String text,
        Long postId
) {
    public static CommentDto fromComment(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getPostId()
        );
    }

    public void validateMandatoryFields() {
        if (this.id == null) {
            throw new MandatoryFieldAbsentException("id");
        }

        if (this.text == null) {
            throw new MandatoryFieldAbsentException("text");
        }

        if (this.postId == null) {
            throw new MandatoryFieldAbsentException("postId");
        }
    }
}
