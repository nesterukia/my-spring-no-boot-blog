package com.nesterukia.blog.dto.comment;

import com.nesterukia.blog.exceptions.MandatoryParameterAbsentException;

public record CreateCommentDto(
        String text,
        Long postId
) {
    public void validateMandatoryFields() {
        if (this.text == null) {
            throw new MandatoryParameterAbsentException("text");
        }

        if (this.postId == null) {
            throw new MandatoryParameterAbsentException("postId");
        }
    }
}
