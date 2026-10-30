package com.ucc.aiproxy.domain;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

/**
 * Estado de consumo de un usuario: plan, contador de requests del minuto actual,
 * tokens usados en el mes y registro diario de consumo.
 * Los métodos son synchronized porque varias peticiones del mismo usuario
 * pueden llegar a la vez.
 */
public class UserAccount {
    private final String userId;
    private Plan plan;
    private int requestsThisMinute;
    private long tokensUsedThisMonth;
    private final Map<LocalDate, DailyUsage> dailyUsage = new TreeMap<>();

    public UserAccount(String userId, Plan plan) {
        this.userId = userId;
        this.plan = plan;
    }

    public synchronized boolean tryConsumeRequest() {
        if (plan.hasUnlimitedRequests()) {
            requestsThisMinute++;
            return true;
        }
        if (requestsThisMinute >= plan.getRequestsPerMinute()) {
            return false;
        }
        requestsThisMinute++;
        return true;
    }

    public synchronized boolean hasTokensFor(long tokens) {
        if (plan.hasUnlimitedTokens()) {
            return true;
        }
        return tokensUsedThisMonth + tokens <= plan.getMonthlyTokens();
    }

    public synchronized void registerUsage(long tokens, LocalDate day) {
        tokensUsedThisMonth += tokens;
        dailyUsage.computeIfAbsent(day, DailyUsage::new).add(tokens);
        // Solo guardamos los últimos 31 días para no crecer sin límite.
        while (dailyUsage.size() > 31) {
            LocalDate oldest = ((TreeMap<LocalDate, DailyUsage>) dailyUsage).firstKey();
            dailyUsage.remove(oldest);
        }
    }

    public synchronized void resetMinute() {
        requestsThisMinute = 0;
    }

    public synchronized void resetMonth() {
        tokensUsedThisMonth = 0;
    }

    public synchronized long getRemainingTokens() {
        if (plan.hasUnlimitedTokens()) {
            return -1;
        }
        return Math.max(0, plan.getMonthlyTokens() - tokensUsedThisMonth);
    }

    public synchronized int getRemainingRequests() {
        if (plan.hasUnlimitedRequests()) {
            return -1;
        }
        return Math.max(0, plan.getRequestsPerMinute() - requestsThisMinute);
    }

    public synchronized DailyUsage getUsageFor(LocalDate day) {
        return dailyUsage.get(day);
    }

    public String getUserId() { return userId; }
    public synchronized Plan getPlan() { return plan; }
    public synchronized void setPlan(Plan plan) { this.plan = plan; }
    public synchronized int getRequestsThisMinute() { return requestsThisMinute; }
    public synchronized long getTokensUsedThisMonth() { return tokensUsedThisMonth; }
}
