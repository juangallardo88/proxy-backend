package com.ucc.aiproxy.domain;

import java.time.LocalDate;

/** Consumo acumulado de un usuario en un día. */
public class DailyUsage {
    private final LocalDate date;
    private long tokens;
    private int requests;

    public DailyUsage(LocalDate date) {
        this.date = date;
    }

    public void add(long tokensUsed) {
        this.tokens += tokensUsed;
        this.requests++;
    }

    public LocalDate getDate() { return date; }
    public long getTokens() { return tokens; }
    public int getRequests() { return requests; }
}
