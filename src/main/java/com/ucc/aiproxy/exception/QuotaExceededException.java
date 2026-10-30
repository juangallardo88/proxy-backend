package com.ucc.aiproxy.exception;

/** Se lanza cuando el usuario agota su cuota mensual de tokens (HTTP 402). */
public class QuotaExceededException extends RuntimeException {
    private final String plan;
    private final long tokensUsed;
    private final long monthlyLimit;
    private final long tokensRequested;

    public QuotaExceededException(String plan, long tokensUsed, long monthlyLimit, long tokensRequested) {
        super("Agotaste la cuota mensual de tokens del plan " + plan
                + " (" + tokensUsed + " de " + monthlyLimit + "). Mejora tu plan para continuar.");
        this.plan = plan;
        this.tokensUsed = tokensUsed;
        this.monthlyLimit = monthlyLimit;
        this.tokensRequested = tokensRequested;
    }

    public String getPlan() { return plan; }
    public long getTokensUsed() { return tokensUsed; }
    public long getMonthlyLimit() { return monthlyLimit; }
    public long getTokensRequested() { return tokensRequested; }
}
