package com.ucc.aiproxy.exception;

import com.ucc.aiproxy.dto.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte las excepciones de los proxies en respuestas HTTP. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** RateLimitProxy -> 429 Too Many Requests + Retry-After */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> rateLimit(RateLimitExceededException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("plan", ex.getPlan());
        details.put("limitPerMinute", ex.getLimit());
        details.put("retryAfterSeconds", ex.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(ErrorResponse.of(429, "Too Many Requests", ex.getMessage(), details));
    }

    /** QuotaProxy -> 402 Payment Required */
    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ErrorResponse> quota(QuotaExceededException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("plan", ex.getPlan());
        details.put("tokensUsed", ex.getTokensUsed());
        details.put("monthlyLimit", ex.getMonthlyLimit());
        details.put("tokensRequested", ex.getTokensRequested());
        details.put("upgradeEndpoint", "/api/quota/upgrade");
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(ErrorResponse.of(402, "Payment Required", ex.getMessage(), details));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getDefaultMessage())
                .orElse("Petición inválida");
        return ResponseEntity.badRequest().body(ErrorResponse.of(400, "Bad Request", message, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, "Bad Request", "El cuerpo de la petición no es un JSON válido", null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(400, "Bad Request", ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> conflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflict", ex.getMessage(), null));
    }
}
