package com.nesterukia.blog.exceptions;

public class MandatoryFieldAbsentException extends RuntimeException {
    public MandatoryFieldAbsentException(String fieldName) {
        super(String.format("Mandatory field '%s' is absent.", fieldName));
    }
}
