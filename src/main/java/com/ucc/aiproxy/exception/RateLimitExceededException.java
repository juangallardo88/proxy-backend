package com.ucc.aiproxy.exception;

/** Se lanza cuando el usuario supera las requests por minuto de su plan (HTTP 429). */
public class RateLimitExceededException extends RuntimeException {
    private final long retryAfterSeconds;
    private final int limit;
    private final String plan;

    public RateLimitExceededException(String plan, int limit, long retryAfterSeconds) {
        super("Superaste el límite de " + limit + " requests por minuto del plan " + plan
                + ". Intenta de nuevo en " + retryAfterSeconds + " segundos.");
        this.plan = plan;
        this.limit = limit;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() { return retryAfterSeconds; }
    public int getLimit() { return limit; }
    public String getPlan() { return plan; }
}
