package com.nesterukia.blog.exceptions;

import com.nesterukia.blog.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MandatoryParameterAbsentException.class)
    public ResponseEntity<ErrorResponse> MandatoryFieldAbsentException(MandatoryParameterAbsentException ex) {
        return formErrorResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        return formErrorResponseEntity(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return formErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    private static ResponseEntity<ErrorResponse> formErrorResponseEntity(HttpStatus status, Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), ex.getLocalizedMessage());
        return new ResponseEntity<>(errorResponse, status);
    }
}

