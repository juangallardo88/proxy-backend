package com.ucc.aiproxy.service;

import com.ucc.aiproxy.domain.DailyUsage;
import com.ucc.aiproxy.domain.Plan;
import com.ucc.aiproxy.domain.UserAccount;
import com.ucc.aiproxy.dto.DailyUsageResponse;
import com.ucc.aiproxy.dto.QuotaStatusResponse;
import com.ucc.aiproxy.dto.UpgradeRequest;
import com.ucc.aiproxy.dto.UpgradeResponse;
import com.ucc.aiproxy.dto.UsageHistoryResponse;
import com.ucc.aiproxy.store.UsageStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Lógica de los endpoints /api/quota/** (estado, historial y upgrade). */
@Service
public class QuotaService {

    /** Promedio aproximado de tokens de una respuesta del mock, para el estimador. */
    public static final long AVERAGE_COMPLETION_TOKENS = 30;

    private static final Locale ES = Locale.forLanguageTag("es-CO");

    private final UsageStore store;

    public QuotaService(UsageStore store) {
        this.store = store;
    }

    public QuotaStatusResponse status(String userId) {
        UserAccount account = store.getOrCreate(userId);
        Plan plan = account.getPlan();
        return new QuotaStatusResponse(
                account.getUserId(),
                plan.name(),
                account.getTokensUsedThisMonth(),
                account.getRemainingTokens(),
                plan.getMonthlyTokens(),
                store.nextMonthlyReset().toOffsetDateTime().toString(),
                account.getRequestsThisMinute(),
                plan.getRequestsPerMinute(),
                account.getRemainingRequests(),
                store.secondsUntilMinuteReset(),
                store.nextMinuteReset().toOffsetDateTime().toString()
        );
    }

    /** Historial de los últimos 7 días (incluye hoy). Los días sin uso salen en 0. */
    public UsageHistoryResponse history(String userId) {
        UserAccount account = store.getOrCreate(userId);
        LocalDate today = store.today();
        List<DailyUsageResponse> days = new ArrayList<>();
        long totalTokens = 0;
        int totalRequests = 0;

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            DailyUsage usage = account.getUsageFor(day);
            long tokens = usage == null ? 0 : usage.getTokens();
            int requests = usage == null ? 0 : usage.getRequests();
            String label = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, ES);
            days.add(new DailyUsageResponse(day.toString(), label, tokens, requests));
            totalTokens += tokens;
            totalRequests += requests;
        }
        return new UsageHistoryResponse(account.getUserId(), account.getPlan().name(), days, totalTokens, totalRequests);
    }

    /**
     * Upgrade de plan con pago simulado. El caso requerido es FREE -> PRO;
     * también se acepta subir a ENTERPRISE. No se permiten bajadas de plan.
     */
    public UpgradeResponse upgrade(String userId, UpgradeRequest request) {
        UserAccount account = store.getOrCreate(userId);
        Plan current = account.getPlan();
        Plan target = parsePlan(request == null ? null : request.getTargetPlan());

        if (target == current) {
            throw new IllegalStateException("Ya tienes el plan " + current.name());
        }
        if (target.ordinal() < current.ordinal()) {
            throw new IllegalArgumentException("No se permite bajar de " + current.name() + " a " + target.name());
        }

        account.setPlan(target);
        String last4 = request == null ? null : sanitizeLast4(request.getCardLast4());
        String method = request == null || request.getPaymentMethod() == null ? "CARD" : request.getPaymentMethod();
        String transactionId = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new UpgradeResponse(
                "Pago simulado aprobado. Ahora tienes el plan " + target.name(),
                current.name(), target.name(), transactionId, method, last4, status(userId));
    }

    private Plan parsePlan(String value) {
        if (value == null || value.isBlank()) {
            return Plan.PRO;
        }
        try {
            return Plan.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Plan no válido: " + value + ". Usa FREE, PRO o ENTERPRISE");
        }
    }

    private String sanitizeLast4(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        return digits.length() >= 4 ? digits.substring(digits.length() - 4) : null;
    }
}
