package com.nesterukia.blog.exceptions;

public record ErrorResponse(int statusCode, String errorMessage) {}