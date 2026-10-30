package com.ucc.aiproxy.dto;

import java.time.Instant;

/** Respuesta del servicio de IA, enriquecida por los proxies con el estado de consumo. */
public class GenerationResponse {
    private String text;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;
    private long latencyMs;
    private Instant createdAt;

    // Campos que completan los proxies
    private String plan;
    private long tokensUsedThisMonth;
    private long tokensRemaining;          // -1 = ilimitado
    private int requestsRemainingThisMinute; // -1 = ilimitado

    public GenerationResponse() {
    }

    public GenerationResponse(String text, long promptTokens, long completionTokens, long latencyMs) {
        this.text = text;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = promptTokens + completionTokens;
        this.latencyMs = latencyMs;
        this.createdAt = Instant.now();
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public long getPromptTokens() { return promptTokens; }
    public void setPromptTokens(long promptTokens) { this.promptTokens = promptTokens; }
    public long getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(long completionTokens) { this.completionTokens = completionTokens; }
    public long getTotalTokens() { return totalTokens; }
    public void setTotalTokens(long totalTokens) { this.totalTokens = totalTokens; }
    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public long getTokensUsedThisMonth() { return tokensUsedThisMonth; }
    public void setTokensUsedThisMonth(long tokensUsedThisMonth) { this.tokensUsedThisMonth = tokensUsedThisMonth; }
    public long getTokensRemaining() { return tokensRemaining; }
    public void setTokensRemaining(long tokensRemaining) { this.tokensRemaining = tokensRemaining; }
    public int getRequestsRemainingThisMinute() { return requestsRemainingThisMinute; }
    public void setRequestsRemainingThisMinute(int v) { this.requestsRemainingThisMinute = v; }
}
