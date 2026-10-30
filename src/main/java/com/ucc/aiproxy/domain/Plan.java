package com.ucc.aiproxy.domain;

/**
 * Planes disponibles en la plataforma.
 * Un valor de -1 significa "ilimitado".
 */
public enum Plan {
    FREE(10, 50_000L),
    PRO(60, 500_000L),
    ENTERPRISE(-1, -1L);

    private final int requestsPerMinute;
    private final long monthlyTokens;

    Plan(int requestsPerMinute, long monthlyTokens) {
        this.requestsPerMinute = requestsPerMinute;
        this.monthlyTokens = monthlyTokens;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public long getMonthlyTokens() {
        return monthlyTokens;
    }

    public boolean hasUnlimitedRequests() {
        return requestsPerMinute < 0;
    }

    public boolean hasUnlimitedTokens() {
        return monthlyTokens < 0;
    }
}
