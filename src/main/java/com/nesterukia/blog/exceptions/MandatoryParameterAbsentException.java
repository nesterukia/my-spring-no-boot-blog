package com.nesterukia.blog.exceptions;

public class MandatoryParameterAbsentException extends RuntimeException {
    public MandatoryParameterAbsentException(String parameterName) {
        super(String.format("Mandatory parameter '%s' is absent.", parameterName));
    }
}
