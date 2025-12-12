package com.nesterukia.blog.dto.post;

import com.nesterukia.blog.exceptions.MandatoryParameterAbsentException;

import java.util.Set;


public record CreatePostDto(
        String title,
        String text,
        Set<String> tags
) {
    public void validateMandatoryFields() {
        if (this.title == null) {
            throw new MandatoryParameterAbsentException("title");
        }

        if (this.text == null) {
            throw new MandatoryParameterAbsentException("text");
        }

        if (this.tags == null) {
            throw new MandatoryParameterAbsentException("tags");
        }
    }
}
