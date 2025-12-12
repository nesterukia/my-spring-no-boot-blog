package com.nesterukia.blog.dto.post;

import com.nesterukia.blog.exceptions.MandatoryParameterAbsentException;

import java.util.Set;

public record UpdatePostDto(
        Long id,
        String title,
        String text,
        Set<String> tags
) {
    public void validateMandatoryFields() {
        if (this.id == null) {
            throw new MandatoryParameterAbsentException("id");
        }

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
