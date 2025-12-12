package com.nesterukia.blog.dto.post;

import com.nesterukia.blog.exceptions.MandatoryFieldAbsentException;

import java.util.Set;

public record UpdatePostDto(
        Long id,
        String title,
        String text,
        Set<String> tags
) {
    public void validateMandatoryFields() {
        if (this.id == null) {
            throw new MandatoryFieldAbsentException("id");
        }

        if (this.title == null) {
            throw new MandatoryFieldAbsentException("title");
        }

        if (this.text == null) {
            throw new MandatoryFieldAbsentException("text");
        }

        if (this.tags == null) {
            throw new MandatoryFieldAbsentException("tags");
        }
    }
}
