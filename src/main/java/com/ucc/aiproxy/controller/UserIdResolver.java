package com.ucc.aiproxy.controller;

/**
 * El frontend identifica al usuario con el header X-User-Id
 * (no hay login en este taller). Se valida para evitar valores raros.
 */
final class UserIdResolver {

    static final String HEADER = "X-User-Id";
    static final String DEFAULT_USER = "demo-user";

    private UserIdResolver() {
    }

    static String resolve(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return DEFAULT_USER;
        }
        String id = headerValue.trim();
        if (!id.matches("[A-Za-z0-9_.@-]{1,64}")) {
            throw new IllegalArgumentException("X-User-Id inválido: usa letras, números, '-', '_', '.' o '@' (máx. 64)");
        }
        return id;
    }
}
