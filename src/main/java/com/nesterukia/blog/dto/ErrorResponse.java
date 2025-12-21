package com.nesterukia.blog.dto;

public record ErrorResponse(int statusCode, String errorMessage) {}