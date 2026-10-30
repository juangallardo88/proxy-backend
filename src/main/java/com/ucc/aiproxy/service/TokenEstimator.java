package com.ucc.aiproxy.service;

/**
 * Estimación simple de tokens: aproximadamente 1 token cada 4 caracteres,
 * la misma regla que usa el frontend para su estimador.
 */
public final class TokenEstimator {

    private TokenEstimator() {
    }

    public static long estimate(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return (long) Math.ceil(text.trim().length() / 4.0);
    }
}
