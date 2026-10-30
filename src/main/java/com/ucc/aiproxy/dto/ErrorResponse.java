package com.ucc.aiproxy.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(int status, String error, String message, Instant timestamp, Map<String, Object> details) {
    public static ErrorResponse of(int status, String error, String message, Map<String, Object> details) {
        return new ErrorResponse(status, error, message, Instant.now(), details);
    }
}
