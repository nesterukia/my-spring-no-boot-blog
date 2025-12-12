package com.nesterukia.blog.dto.comment;

import com.nesterukia.blog.exceptions.MandatoryFieldAbsentException;

public record CreateCommentDto(
        String text,
        Long postId
) {
    public void validateMandatoryFields() {
        if (this.text == null) {
            throw new MandatoryFieldAbsentException("text");
        }

        if (this.postId == null) {
            throw new MandatoryFieldAbsentException("postId");
        }
    }
}
